package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocAccountType;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.gosuslugi.dom.schema.integration.house_management.AccountIndType;
import ru.gosuslugi.dom.schema.integration.house_management.AccountType;
import ru.gosuslugi.dom.schema.integration.house_management.ImportAccountRequest;
import ru.gosuslugi.dom.schema.integration.individual_registry_base.ID;
import ru.gosuslugi.dom.schema.integration.organizations_registry_base.RegOrgVersionType;

public class AccountLog extends GisWsLogTable {

    public AccountLog () {

        super ("tb_accounts__log", "История редактирования лицевых счетов", Account.class
            , EnTable.c.class
            , Account.c.class
        );

    }

    public Get getForExport (String id) {
        
        final NsiTable nsi95 = NsiTable.getNsiTable (95);

        return (Get) getModel ()
                
            .get (this, id, "*")
                
            .toOne (Account.class, "AS r"
                , Account.c.ID_CTR_STATUS.lc ()
                , Account.c.ID_TYPE.lc ()
                , Account.c.IS_CUSTOMER_ORG.lc ()
            ).on ()
                
            .toMaybeOne (VocOrganization.class, "AS org", "orgppaguid AS orgppaguid").on ("r.uuid_org=org.uuid")
                
            .toMaybeOne (VocOrganization.class, "AS org_customer", "orgversionguid AS orgversionguid").on ("r.uuid_org_customer=org_customer.uuid")
                
            .toMaybeOne (VocPerson.class, "AS ind", "*").on ("r.uuid_org=org.uuid")
            .toOne      (nsi95, nsi95.getLabelField ().getfName () + " AS vc_nsi_95", "code", "guid").on ("(ind.code_vc_nsi_95=vc_nsi_95.code AND vc_nsi_95.isactual=1)")
                
            .toMaybeOne (Contract.class, "AS ca"
                , "contractguid AS contractguid"
            ).on ()                
                
            .toMaybeOne (Charter.class, "AS ch"
                , "charterguid AS charterguid"
            ).on ()

        ;
        
    }
    
    public static void addItemsForExport (DB db, Map<String, Object> r) throws SQLException {

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
        
        if (DB.ok (r.get ("r.is_customer_org"))) {
            result.setOrg (toOrg (r));
        }
        else {
            result.setInd (toInd (r));
        }
        
        return result;
        
    }

    private static RegOrgVersionType toOrg (Map<String, Object> r) {
        final RegOrgVersionType result = DB.to.javaBean (RegOrgVersionType.class, r);
        return result;
    }

    private static AccountIndType toInd (Map<String, Object> r) {
        
        Map<String, Object> rr = DB.HASH ();
        
        r.entrySet ().forEach ((kv) -> {
            if (kv.getKey ().startsWith ("ind.")) rr.put (kv.getKey ().substring (4), kv.getValue ());
        });
        
        final AccountIndType result = DB.to.javaBean (AccountIndType.class, rr);

        if (result.getSNILS () == null && DB.ok (r.get ("ind.code_vc_nsi_95"))) result.setID (toID (r, rr));
        
        return result;
        
    }

    private static ID toID (Map<String, Object> r, Map<String, Object> rr) {
        rr.put ("number", rr.get ("number_"));
        final ID result = DB.to.javaBean (ID.class, rr);
        result.setType (NsiTable.toDom (r, "vc_nsi_95"));
        return result;
    }
    
}