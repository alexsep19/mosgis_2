package ru.eludia.products.mosgis.jms.gis.poll;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;
import javax.json.JsonObject;
import javax.json.JsonString;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState.i.DONE;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFileLog;
import ru.eludia.products.mosgis.db.model.incoming.xl.lines.InXlOrgPackItem;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollException;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollMDB;
import ru.eludia.products.mosgis.jms.gis.poll.base.GisPollRetryException;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common_service_async.Fault;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationLog;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationNsi20;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationTypes;
import ru.eludia.products.mosgis.ws.soap.clients.WsGisOrgCommonClient;
import ru.eludia.products.mosgis.ws.soap.tools.SOAPTools;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiRef;
import ru.gosuslugi.dom.schema.integration.organizations_registry_base.SubsidiaryType;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common.ExportOrgRegistryResultType;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common.GetStateResult;


@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inXlOrgPackPollQueue")
 , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
 , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class GisPollExportOrgPackMDB  extends GisPollMDB {

    @EJB
    protected WsGisOrgCommonClient wsGisOrgClient;
    
    @Resource (mappedName = "mosgis.inXlOrgPackCheckQueue")            
    Queue inXlOrgPackCheckQueue;        

    @Override
    protected Get get (UUID uuid) {
        return (Get) ModelHolder.getModel ().get (getTable (), uuid, "AS root", "*")
            .toOne (InXlFileLog.class, "AS log", "uuid", "uuid_object").on ("log.uuid_out_soap=root.uuid")
        ;
    }
    
    protected final XSSFWorkbook readWorkbook (DB db, UUID uuid) throws SQLException {
        
        XSSFWorkbook [] x = new XSSFWorkbook [] {null};
        
        db.forFirst (db.getModel ().get (InXlFile.class, uuid, "body"), (rs) -> {
            
            try {
                x [0] = new XSSFWorkbook (rs.getBlob (1).getBinaryStream ());
            }
            catch (IOException ex) {
                throw new IllegalStateException (ex);
            }

        });
        
        return x [0];
        
    }        

    @Override
    protected void handleOutSoapRecord (DB db, UUID uuid, Map<String, Object> r) throws SQLException {
        
        if (DB.ok (r.get ("is_failed"))) throw new IllegalStateException (r.get ("err_text").toString ());
                                
        try {
            
            db.update (InXlOrgPackItem.class, HASH (
                InXlOrgPackItem.c.UUID_PACK, uuid,
                InXlOrgPackItem.c.LABEL, "(не найдено)"
            )
                , InXlOrgPackItem.c.UUID_PACK.lc ()
            );
            
            GetStateResult state = getState (r);
            
            ErrorMessageType errorMessage = state.getErrorMessage ();
            
            if (errorMessage != null) throw new GisPollException (errorMessage);
            
            final List<ExportOrgRegistryResultType> exportOrgRegistryResult = state.getExportOrgRegistryResult ();
            
            if (exportOrgRegistryResult == null) throw new GisPollException ("0", "Сервис ГИС ЖКХ вернул пустой результат");
                                                
            for (ExportOrgRegistryResultType i: exportOrgRegistryResult) if (handleOrgRegistryResult (i.getOrgVersion (), i, db, uuid)) continue;

            db.update (OutSoap.class, HASH (
                "uuid", getUuid (),
                "id_status", DONE.getId ()
            ));
            
            UUID uuidXlFile = (UUID) r.get ("log.uuid_object");

            uuidPublisher.publish (inXlOrgPackCheckQueue, uuidXlFile);
            
            XSSFWorkbook wb = readWorkbook (db, uuidXlFile);
            
            final XSSFSheet sheet = wb.getSheet ("Шаблон добавления организаций");        
            
            List<Map<String, Object>> list = db.getList (db.getModel ()
                .select (InXlOrgPackItem.class, "*")
                .where  (InXlOrgPackItem.c.UUID_PACK, uuid)
                .where  (InXlOrgPackItem.c.ERR.lc () + " IS NULL")
            );
            
            list.forEach ((t) -> {
                sheet
                    .getRow ((int) DB.to.Long (t.get (InXlOrgPackItem.c.ORD.lc ())) - 1)
                        .getCell (2)
                            .setCellValue (DB.to.String (t.get ("label")));
            });
            
            final Connection cn = db.getConnection ();

            cn.setAutoCommit (false);

            try (PreparedStatement st = cn.prepareStatement ("SELECT errr FROM in_xl_files WHERE uuid = ? FOR UPDATE")) {

                st.setString (1, uuid.toString ().replace ("-", "").toUpperCase ());

                try (ResultSet rs = st.executeQuery ()) {

                    if (rs.next ()) {

                        Blob blob = rs.getBlob (1);

                        try (OutputStream os = blob.setBinaryStream (0L)) {
                            wb.write (os);
                        }
                        catch (IOException ex) {
                            logger.log (Level.SEVERE, "Cannot store errors", ex);
                        }

                    }

                }

                cn.commit ();
                cn.setAutoCommit (true);

            }

        }
        catch (GisPollRetryException ex) {
            return;
        }
        catch (GisPollException ex) {            
            ex.register (db, uuid, r);
        }
        
    }

    private GetStateResult getState (Map<String, Object> r) throws GisPollRetryException, GisPollException {

        GetStateResult rp;
        
        try {
            rp = wsGisOrgClient.getState ((UUID) r.get ("uuid_ack"));
        }
        catch (Fault ex) {
            throw new GisPollException (ex.getFaultInfo ());
        }
        catch (Throwable ex) {            
            throw new GisPollException (ex);
        }
        
        checkIfResponseReady (rp);
        
        return rp;
        
    }    
    
    
    
    
    
    
    
    private boolean handleOrgRegistryResult (ExportOrgRegistryResultType.OrgVersion orgVersion, ExportOrgRegistryResultType i, DB db, UUID uuidOutSoap) throws SQLException, GisPollException {
        
        Object o = null;
        String parentOrgGuid = null;
        SubsidiaryType.SourceName source = null;
        
        if (orgVersion.getLegal () != null) o = orgVersion.getLegal ();
        if (orgVersion.getEntrp () != null) o = orgVersion.getEntrp ();
        if (orgVersion.getSubsidiary () != null) {
            o = orgVersion.getSubsidiary ();
            
            source = orgVersion.getSubsidiary().getSourceName();
            
            String parentOrgVersionGuid = orgVersion.getSubsidiary().getParentOrg().getRegOrgVersion().getOrgVersionGUID();
            parentOrgGuid = db.getString(db.getModel()
                    .select(VocOrganization.class, "uuid")
                    .where(VocOrganization.c.ORGVERSIONGUID, parentOrgVersionGuid));
        }
        
        if (orgVersion.getForeignBranch () != null) o = orgVersion.getForeignBranch ();
        
        if (o == null) throw new GisPollException ("0", "Unknown org type");

        JsonObject jo = DB.to.JsonObject (SOAPTools.toJSON (o));

        final String uuid = i.getOrgRootEntityGUID ();
        
        Map<String, Object> record = DB.HASH (
            VocOrganization.c.ID_TYPE,           VocOrganizationTypes.i.valueOf (o).getId (),
            VocOrganization.c.ORGROOTENTITYGUID, uuid,
            VocOrganization.c.ORGVERSIONGUID,    orgVersion.getOrgVersionGUID(),
            VocOrganization.c.ORGPPAGUID,        i.getOrgPPAGUID (),
            VocOrganization.c.LASTEDITINGDATE,   orgVersion.getLastEditingDate(),
            VocOrganization.c.ISACTUAL,          orgVersion.isIsActual(),
            VocOrganization.c.PARENT,            parentOrgGuid,
            VocOrganization.c.ISREGISTERED,      i.isIsRegistered() != null ? i.isIsRegistered() : false
        );
        
        if (source != null) {
            record.put(VocOrganization.c.SOURCENAME.lc(), source.getValue());
            record.put(VocOrganization.c.SOURCEDATE.lc(), source.getDate());
        }
        
        jo.forEach ((k, v) -> {
            
            if (!(v instanceof JsonString)) return;
            
            String f = k.toLowerCase ();
            
            if ("ogrnip".equals (f) || "nza".equals (f)) f = "ogrn";
            
            record.put (f, ((JsonString) v).getString ());
            
        });
                
        db.upsert (VocOrganization.class, record);
        
        record.put ("uuid_object", uuid);
        record.put ("uuid_out_soap", uuidOutSoap);
        record.put ("action", VocAction.i.REFRESH);
        
        UUID uuidOrg = (UUID) db.insertId (VocOrganizationLog.class, record);
        
        Map<String, Object> org = db.getMap (db.getModel ().get (VocOrganizationLog.class, uuidOrg, "*"));        
        
        db.update (InXlOrgPackItem.class, HASH (
            InXlOrgPackItem.c.UUID_PACK, uuidOutSoap,
            InXlOrgPackItem.c.OGRN, org.get ("ogrn"),
            InXlOrgPackItem.c.LABEL, org.get ("label")
//            InXlOrgPackItem.c.KPP, record.get ("kpp"),
        )
            , InXlOrgPackItem.c.UUID_PACK.lc ()
            , InXlOrgPackItem.c.OGRN.lc ()
        );
        
        boolean isUo = false;
        
        List<Map<String, Object>> roles = new ArrayList<>();
        for (NsiRef role : i.getOrganizationRoles()) {
            if ("1".equals(role.getCode()))
                isUo = true;
            
            roles.add(HASH(
                "is_deleted", 0,
                "code", role.getCode()
            ));
        }
        
        db.dupsert (
            VocOrganizationNsi20.class, 
            HASH ("uuid", uuid), 
            roles,
            "code"
        );
/*
        if (isUo) {
            uuidPublisher.publish(inExportLicenseQueue, (UUID) db.insertId(InLicenses.class, DB.HASH("uuid_org", uuid)));
        }
*/        
        return false;

    }
    
}