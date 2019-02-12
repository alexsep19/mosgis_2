package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisFileLogTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.gosuslugi.dom.schema.integration.house_management.ImportAccountIndividualServicesRequest;

public class AccountIndividualServiceLog extends GisFileLogTable {

    public AccountIndividualServiceLog () {

        super ("tb_account_svc__log", "История редактирования индивидуальных услуг лицевых счетов", AccountIndividualService.class
            , EnTable.c.class
            , AttachTable.c.class
            , AccountIndividualService.c.class
        );
        
    }
    
    public Get getForExport (String id) {
        
        return (Get) getModel ()
                
            .get (this, id, "*")
                
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
    
    private static ImportAccountIndividualServicesRequest.IndividualService toIndividualService (Map<String, Object> r) {
        final ImportAccountIndividualServicesRequest.IndividualService result = DB.to.javaBean (ImportAccountIndividualServicesRequest.IndividualService.class, r);
        result.setTransportGUID (UUID.randomUUID ().toString ());
        return result;
    }
    
}