package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.gosuslugi.dom.schema.integration.house_management.ImportAccountRequest;

public class AccountLog extends GisWsLogTable {

    public AccountLog () {

        super ("tb_accounts__log", "История редактирования лицевых счетов", Account.class
            , EnTable.c.class
            , Account.c.class
        );

    }

    public Get getForExport (String id) {

        return (Get) getModel ()
                
            .get (this, id, "*")
                
            .toOne (Account.class, "AS r"
                , Account.c.ID_CTR_STATUS.lc ()
                , Account.c.ID_TYPE.lc ()
            ).on ()
                
            .toMaybeOne (VocOrganization.class, "AS org", "orgppaguid AS orgppaguid").on ("r.uuid_org=org.uuid")
                
            .toMaybeOne (Contract.class, "AS ca"
                , "contractguid AS contractguid"
            ).on ()                
                
            .toMaybeOne (Charter.class, "AS ch"
                , "charterguid AS charterguid"
            ).on ()

        ;
        
    }

    public static ImportAccountRequest toImportAccountRequest (Map<String, Object> r) {
        final ImportAccountRequest result = DB.to.javaBean (ImportAccountRequest.class, r);
        result.getAccount ().add (toAccount (r));
        return result;
    }    

    private static ImportAccountRequest.Account toAccount (Map<String, Object> r) { 
        final ImportAccountRequest.Account result = DB.to.javaBean (ImportAccountRequest.Account.class, r);
        return result;
    }

}