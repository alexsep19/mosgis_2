package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.voc.VocAccountType;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.gosuslugi.dom.schema.integration.house_management.AccountType;
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
    
    public static void addPlannedWorksForExport (DB db, Map<String, Object> r) throws SQLException {

        r.put ("items", db.getList (db.getModel ()                
            .select (AccountItem.class, "*")
            .toMaybeOne (Premise.class, "AS prem"
                , "livingroomguid AS livingroomguid"
                , "premisesguid AS premisesguid"
            ).on ()
            .where  (AccountItem.c.UUID_ACCOUNT, r.get ("uuid_object"))
            .and    ("is_deleted", 0)
        ));

    }    

    public static ImportAccountRequest toImportAccountRequest (Map<String, Object> r) {
        final ImportAccountRequest result = DB.to.javaBean (ImportAccountRequest.class, r);
        result.getAccount ().add (toAccount (r));
        return result;
    }    

    private static ImportAccountRequest.Account toAccount (Map<String, Object> r) {         
        r.put (VocAccountType.i.forId (r.get ("r.id_type")).getFlagName (), 1);        
        final ImportAccountRequest.Account result = DB.to.javaBean (ImportAccountRequest.Account.class, r);
        for (Map<String, Object> i: (List<Map<String, Object>>) r.get ("items")) result.getAccommodation ().add (AccountItem.toAccommodation (i));
        result.setTransportGUID (UUID.randomUUID ().toString ());
        result.setPayerInfo (toPayerInfo (r));
        return result;
    }
    
    private static AccountType.PayerInfo toPayerInfo (Map<String, Object> r) {
        final AccountType.PayerInfo result = DB.to.javaBean (AccountType.PayerInfo.class, r);
        if (Boolean.FALSE.equals (result.isIsAccountsDivided ())) result.setIsAccountsDivided (null);
        if (Boolean.FALSE.equals (result.isIsRenter ())) result.setIsRenter (null);
        return result;
    }

}