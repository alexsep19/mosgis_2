package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisFileLogTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.gosuslugi.dom.schema.integration.house_management.ImportAccountIndividualServicesRequest;

public class AccountIndividualServiceLog extends GisFileLogTable {

    public AccountIndividualServiceLog () {

        super ("tb_account_svc__log", "История редактирования индивидуальных услуг лицевых счетов", AccountIndividualService.class
            , EnTable.c.class
            , AttachTable.c.class
            , AccountIndividualService.c.class
        );
        
        fk    ("uuid_out_soap",             OutSoap.class,                      null,   "Последний запрос на импорт в ГИС ЖКХ");
        col   ("uuid_message",              Type.UUID,                          null,   "UUID запроса в ГИС ЖКХ");
        
    }
    
    public Get getForExport (String id) {
        
        return (Get) getModel ()
                
            .get (this, id, "*")
                
            .toOne (AdditionalService.class, "AS add_service", "code", "guid").on ()
                
            .toOne (AccountIndividualService.class, "AS r"
                , EnTable.c.UUID.lc ()
                , AccountIndividualService.c.ID_CTR_STATUS.lc ()
            ).on ()

            .toOne (Account.class, "AS acc"
                , Account.c.ACCOUNTGUID.lc () + " AS accountguid"
            ).on ()
                
            .toOne (VocOrganization.class, "AS org", "orgppaguid AS orgppaguid").on ("acc.uuid_org=org.uuid")                
                
        ;
        
    }    
    
    public static ImportAccountIndividualServicesRequest toImportAccountIndividualServicesRequest (Map<String, Object> r) {
        final ImportAccountIndividualServicesRequest result = new ImportAccountIndividualServicesRequest ();
        result.getIndividualService ().add (toIndividualService (r));
        return result;
    }
    
    public static ImportAccountIndividualServicesRequest toDeleteAccountIndividualServicesRequest (Map<String, Object> r) {
        final ImportAccountIndividualServicesRequest result = new ImportAccountIndividualServicesRequest ();
        result.getDeleteIndividualService ().add (toDeleteIndividualService (r));
        return result;
    }    
    
    private static ImportAccountIndividualServicesRequest.IndividualService toIndividualService (Map<String, Object> r) {
        final ImportAccountIndividualServicesRequest.IndividualService result = DB.to.javaBean (ImportAccountIndividualServicesRequest.IndividualService.class, r);
        if (result.getAccountIndividualServiceGUID () != null) result.setAccountGUID (null);
        result.setTransportGUID (UUID.randomUUID ().toString ());
        result.setAdditionalService (NsiTable.toDom (r, "add_service"));
        result.setAttachment (AccountIndividualService.toAttachmentType (r));                
        return result;
    }
    
    private static ImportAccountIndividualServicesRequest.DeleteIndividualService toDeleteIndividualService (Map<String, Object> r) {
        final ImportAccountIndividualServicesRequest.DeleteIndividualService result = DB.to.javaBean (ImportAccountIndividualServicesRequest.DeleteIndividualService.class, r);
        result.setTransportGUID (UUID.randomUUID ().toString ());
        return result;
    }

}