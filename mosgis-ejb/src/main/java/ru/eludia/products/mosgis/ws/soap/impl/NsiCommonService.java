package ru.eludia.products.mosgis.ws.soap.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.datatype.XMLGregorianCalendar;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.nsi.NsiMultipleRefTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiMultipleScalarTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.nsi.fields.NsiField;
import ru.eludia.products.mosgis.db.model.nsi.fields.NsiNsiRefField;
import ru.eludia.products.mosgis.db.model.nsi.fields.NsiScalarField;
import ru.eludia.products.mosgis.db.model.voc.VocNsiList;
import ru.eludia.products.mosgis.db.model.voc.VocNsiListGroup;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.gosuslugi.dom.schema.integration.nsi.ExportNsiPagingItemResult;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiItemInfoType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiListType;
import ru.gosuslugi.dom.schema.integration.nsi_common_service.Fault;
import ru.gosuslugi.dom.schema.integration.nsi_common.ExportNsiListResult;
import ru.gosuslugi.dom.schema.integration.nsi_common.ExportNsiItemResult;

@HandlerChain (file="handler-chain.xml")
@WebService (
    serviceName = "NsiService", 
    portName = "NsiPort", 
    endpointInterface = "ru.gosuslugi.dom.schema.integration.nsi_common_service.NsiPortsType", 
    targetNamespace = "http://dom.gosuslugi.ru/schema/integration/nsi-common-service/", 
    wsdlLocation = "META-INF/wsdl/nsi-common/hcs-nsi-common-service.wsdl"
)
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class NsiCommonService {    
    
    protected Logger logger = Logger.getLogger (getClass ().getName ());

    private static final XMLGregorianCalendar epoch = DB.to.XMLGregorianCalendar (new java.sql.Timestamp (0L));

    public ExportNsiListResult exportNsiList (ru.gosuslugi.dom.schema.integration.nsi_common.ExportNsiListRequest exportNsiListRequest) throws Fault {
        
        ExportNsiListResult result = new ExportNsiListResult ();

        String listGroup = exportNsiListRequest.getListGroup ();
        
        VocNsiListGroup.i group = VocNsiListGroup.i.forName (listGroup);
        
        if (group == null) throw new IllegalArgumentException ("Invalid group name + '" + listGroup + "'");
        
        final NsiListType list = new NsiListType ();
        list.setListGroup (listGroup);
        list.setCreated (epoch);                
        
        List<NsiItemInfoType> nsiItemInfo = list.getNsiItemInfo ();
        
        Model m = ModelHolder.getModel ();
       
        try (DB db = m.getDb ()) {            
            db.forEach (m
                .select (VocNsiList.class, "*")
                .where ("listgroup", group.getName ())
                .and   ("cols IS NOT NULL")
                ,(rs) -> {nsiItemInfo.add (VocNsiList.toDom (db, rs));}
            );
        }
        catch (Exception ex) {
           throw new IllegalStateException (ex);
        }        
        
        result.setNsiList (list);
        
        return result;
        
    }

    public ExportNsiItemResult exportNsiItem (ru.gosuslugi.dom.schema.integration.nsi_common.ExportNsiItemRequest exportNsiItemRequest) throws Fault {
        
        ExportNsiItemResult result = new ExportNsiItemResult ();        
        final ExportNsiPagingItemResult.NsiItem nsiItem = new ExportNsiPagingItemResult.NsiItem ();

        BigInteger registryNumber = exportNsiItemRequest.getRegistryNumber ();
                
        nsiItem.setCreated (epoch);        
        nsiItem.setNsiItemRegistryNumber (registryNumber);

        List<NsiElementType> nsiElement = nsiItem.getNsiElement ();
        
        Model m = ModelHolder.getModel ();

        try (DB db = m.getDb ()) {                        
            
            NsiTable nsiTable = NsiTable.getNsiTable (registryNumber.intValue ());
            
            final Select select = m.select (nsiTable, "AS root", "*");
            
            for (NsiField f: nsiTable.getNsiFields ().values ()) {
                
                if (f.isMultiple () || !(f instanceof NsiNsiRefField)) continue;

                NsiNsiRefField nrf = (NsiNsiRefField) f;
                
                int rn = nrf.getRegistryNumber ();
                
                NsiTable refTable = NsiTable.getNsiTable (rn);
                
                String alias = "nsi_" + rn;
                
                select.toMaybeOne (refTable, "AS " + alias, "code").on ("root." + nrf.getfName () + "=" + alias + ".guid");
                
            }
                                    
            Map<Object, Map<String, Object>> idx = db.getIdx (select);
            
            for (NsiMultipleScalarTable mst: nsiTable.getMultipleScalarTables ()) {
                
                db.adjustTable (mst);
                
                final NsiScalarField valueField = mst.getValueField ();
                
                String name = valueField.getName ();
                String fName = valueField.getfName ().toLowerCase ();

                db.forEach (m.select (mst, "*").orderBy ("ord")                        

                    , (rs) -> {
                        
                        Map<String, Object> r = db.HASH (rs);

                        final Object parentId = r.get ("guid");

                        if (parentId == null) return;

                        Map<String, Object> parent = idx.get (parentId);

                        if (parent == null) {
                            logger.warning ("parent not found for " + parentId);
                            return;
                        };

                        if (!parent.containsKey (name)) parent.put (name, new ArrayList ());

                        ((List) parent.get (name)).add (r.get (fName));
                        
                    }
                        
                );
                                
            }
            
            for (NsiMultipleRefTable mrt: nsiTable.getMultipleRefTables ()) {
                
                db.adjustTable (mrt);
                
                String name = mrt.getTargetField ().getName ();
                
                NsiTable refTable = NsiTable.getNsiTable (mrt.getTargetField ().getRegistryNumber ());
                
                db.forEach (m
                    
                    .select (mrt,      "AS root", "guid_from"   )
                    .toOne  (refTable, "AS ref",  "code", "guid").on ("root.guid_to=ref.guid")
                    .orderBy ("root.ord")
                    
                    , (rs) -> {
                    
                    Map<String, Object> r = db.HASH (rs);
                    
                    final Object parentId = r.get ("guid_from");
                    
                    if (parentId == null) return;
                    
                    Map<String, Object> parent = idx.get (parentId);
                    
                    if (parent == null) {
                        logger.warning ("parent not found for " + parentId);
                        return;
                    };
                                        
                    if (!parent.containsKey (name)) parent.put (name, new ArrayList ());
                    
                    ((List) parent.get (name)).add (r);
                    
                });
                
            }
            
            if (nsiTable.getColumns ().containsKey ("parent")) {
                                
                for (Map<String, Object> r: idx.values ()) {
                    
                    final Object parentId = r.get ("parent");
                    
                    if (parentId == null) continue;
                    
                    Map<String, Object> parent = idx.get (parentId);
                    
                    if (parent == null) {
                        logger.warning ("parent not found for " + parentId);
                        continue;
                    };
                                        
                    if (!parent.containsKey (NsiTable.CHILDREN)) parent.put (NsiTable.CHILDREN, new ArrayList ());
                    
                    ((List) parent.get (NsiTable.CHILDREN)).add (r);

                }
                
            }
            
            for (Map<String, Object> r: idx.values ()) if (r.get ("parent") == null) nsiElement.add (nsiTable.toDom (r));
            
        }
        catch (Exception ex) {
           throw new IllegalStateException (ex);
        }                
        
        result.setNsiItem (nsiItem);
        
        return result;
                
    }

    public ru.gosuslugi.dom.schema.integration.nsi_common.ExportNsiPagingItemResult exportNsiPagingItem (ru.gosuslugi.dom.schema.integration.nsi_common.ExportNsiPagingItemRequest exportNsiPagingItemRequest) throws Fault {
        throw new UnsupportedOperationException ("Not to be implemented.");
    }
    
}
