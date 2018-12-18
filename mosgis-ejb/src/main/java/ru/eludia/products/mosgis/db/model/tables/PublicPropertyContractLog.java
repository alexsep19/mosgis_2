package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.eludia.products.mosgis.db.model.voc.VocPublicPropertyContractFileType;
import ru.gosuslugi.dom.schema.integration.house_management.ImportPublicPropertyContractRequest;
import ru.gosuslugi.dom.schema.integration.house_management.PublicPropertyContractType;
import ru.gosuslugi.dom.schema.integration.individual_registry_base.ID;
import ru.gosuslugi.dom.schema.integration.individual_registry_base.IndType;

public class PublicPropertyContractLog extends GisWsLogTable {
    
    public PublicPropertyContractLog () {

        super ("tb_pp_ctr__log", "История редактирования [сведений о размере платы за] услуги управления", PublicPropertyContract.class
            , EnTable.c.class
            , PublicPropertyContract.c.class
        );
        
    }
    
    public static ImportPublicPropertyContractRequest toImportPublicPropertyContractRequest (Map<String, Object> r) {
        final ImportPublicPropertyContractRequest createImportPublicPropertyContractRequest = new ImportPublicPropertyContractRequest ();
        final ImportPublicPropertyContractRequest.Contract contract = new ImportPublicPropertyContractRequest.Contract ();
        final ImportPublicPropertyContractRequest.Contract.PublicPropertyContract publicPropertyContract = toContractPublicPropertyContract (r);
        contract.setPublicPropertyContract (publicPropertyContract);
        contract.setTransportGUID (UUID.randomUUID ().toString ());
        createImportPublicPropertyContractRequest.getContract ().add (contract);
        return createImportPublicPropertyContractRequest;
    }
    
    private static ImportPublicPropertyContractRequest.Contract.PublicPropertyContract toContractPublicPropertyContract (Map<String, Object> r) {
        
        r.put ("date", r.get ("date_"));
        
        ImportPublicPropertyContractRequest.Contract.PublicPropertyContract result = DB.to.javaBean (ImportPublicPropertyContractRequest.Contract.PublicPropertyContract.class, r);
        
        if (Boolean.FALSE.equals (result.isIsGratuitousBasis ())) result.setIsGratuitousBasis (null);
        
        UUID uuidOrg = (UUID) r.get ("ctr.uuid_org_customer");
        if (uuidOrg != null) {
            result.setOrganization (VocOrganization.regOrgType (uuidOrg));
        }
        else {
            Map<String, Object> pr = DB.HASH ();
            for (Map.Entry<String, Object> i: r.entrySet ()) {
                String k = i.getKey ();
                if (k.startsWith ("p."))         pr.put (k.substring (2), i.getValue ());
                if (k.startsWith ("vc_nsi_95.")) pr.put (k, i.getValue ());
            }
            pr.put ("number", pr.get ("number_"));
            result.setEntrepreneur (toIndType (pr));
        }
        
        List <Map <String, Object>> files = (List <Map <String, Object>>) r.get ("files");
        if (files == null) throw new IllegalStateException ("No files fetched: " + r);
        
        for (Map <String, Object> file: files) {
            
            switch (VocPublicPropertyContractFileType.i.forId (DB.to.Long (file.get ("id_type")))) {
                case ADDENDUM:
                case CONTRACT:
                    result.getContractAttachment ().add (AttachTable.toAttachmentType (file));
                    break;
                case VOTING_PROTO:
                    result.getRentAgrConfirmationDocument ().add (PublicPropertyContractFile.toRentAgrConfirmationDocument (file));
                    break;
                default:
                    throw new IllegalStateException ("Invalid file type: " + r);
            }
            
        }
        
        return result;
        
    }

    private static IndType toIndType (Map<String, Object> r) {
        
        final IndType result = DB.to.javaBean (IndType.class, r);
        
        if (DB.ok (r.get ("vc_nsi_95.code"))) {
            result.setID (toID (r));
            result.setSNILS (null);
        }
        
        return result;
        
    }

    private static ID toID (Map<String, Object> r) {
        final ID id = DB.to.javaBean (ID.class, r);
        id.setType (NsiTable.toDom (r, "vc_nsi_95"));
        return id;
    }

    public Get getForExport (Object id) {
        
        NsiTable nsi95 = NsiTable.getNsiTable (95);        
        
        return (Get) getModel ()
            .get (this, id, "*")
            .toOne (PublicPropertyContract.class, "AS ctr"
                , PublicPropertyContract.c.FIASHOUSEGUID.lc () + " AS fiashouseguid"
                , PublicPropertyContract.c.UUID_ORG_CUSTOMER.lc ()
            ).on ()
            .toMaybeOne (VocPerson.class, "AS p", "*").on ("ctr.uuid_person_customer=p.uuid")
            .toMaybeOne (nsi95, "AS vc_nsi_95", "code", "guid").on ("vc_nsi_95.code=p.code_vc_nsi_95 AND vc_nsi_95.isactual=1")
        ;
        
    }
    
    public static void addFilesForExport (DB db, Map<String, Object> r) throws SQLException {
        
        r.put ("files", db.getList (db.getModel ()
            .select (PublicPropertyContractFile.class, "*")
            .where  (PublicPropertyContractFile.c.UUID_CTR.lc (), r.get ("uuid_object"))
            .toOne  (PublicPropertyContractFileLog.class, "AS log", "ts_start_sending", "err_text").on ()
            .and    ("id_status", 1)
        ));

    }
                
}