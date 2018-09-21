package ru.eludia.products.mosgis.web;

import java.util.List;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.datatype.XMLGregorianCalendar;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.products.mosgis.db.model.voc.VocNsiList;
import ru.eludia.products.mosgis.db.model.voc.VocNsiListGroup;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiItemInfoType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiListType;
import ru.gosuslugi.dom.schema.integration.nsi_common_service.Fault;
import ru.gosuslugi.dom.schema.integration.nsi_common.ExportNsiListResult;

@WebService (
    serviceName = "NsiService", 
    portName = "NsiPort", 
    endpointInterface = "ru.gosuslugi.dom.schema.integration.nsi_common_service.NsiPortsType", 
    targetNamespace = "http://dom.gosuslugi.ru/schema/integration/nsi-common-service/", 
    wsdlLocation = "META-INF/wsdl/nsi-common/hcs-nsi-common-service.wsdl"
)
@Stateless
public class NsiCommonService {    
    
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
                ,(rs) -> {nsiItemInfo.add (VocNsiList.toDom (db, rs));}
            );
        }
        catch (Exception ex) {
           throw new IllegalStateException (ex);
        }        
        
        result.setNsiList (list);
        
        return result;
        
    }

    public ru.gosuslugi.dom.schema.integration.nsi_common.ExportNsiItemResult exportNsiItem (ru.gosuslugi.dom.schema.integration.nsi_common.ExportNsiItemRequest exportNsiItemRequest) throws Fault {
        //TODO implement this method
        throw new UnsupportedOperationException ("Not implemented yet.");
    }

    public ru.gosuslugi.dom.schema.integration.nsi_common.ExportNsiPagingItemResult exportNsiPagingItem (ru.gosuslugi.dom.schema.integration.nsi_common.ExportNsiPagingItemRequest exportNsiPagingItemRequest) throws Fault {
        throw new UnsupportedOperationException ("Not to be implemented.");
    }
    
}
