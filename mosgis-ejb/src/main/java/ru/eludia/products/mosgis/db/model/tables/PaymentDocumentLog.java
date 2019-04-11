package ru.eludia.products.mosgis.db.model.tables;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.voc.VocChargeInfoType;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi2;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi329;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi331;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi50;
import ru.gosuslugi.dom.schema.integration.bills.ImportPaymentDocumentRequest;
import ru.gosuslugi.dom.schema.integration.bills.PaymentDocumentType;

public class PaymentDocumentLog extends GisWsLogTable {

    private static final String __ACCT2TOTAL = "__acct2total";
    
    public PaymentDocumentLog () {

        super (PaymentDocument.TABLE_NAME + "__log", "История редактирования платёжных документов", PaymentDocument.class
            , EnTable.c.class
            , PaymentDocument.c.class
        );

    }
    
    public static Map<String, Object> getForExport (DB db, String id) throws SQLException {
        
        final Model m = db.getModel ();
        
        final Map<String, Object> r = db.getMap (m                
            .get (PaymentDocumentLog.class, id, "*")
            .toOne (PaymentDocument.class, "AS r"
                , EnTable.c.UUID.lc ()
                , PaymentDocument.c.ID_TYPE.lc ()
                , PaymentDocument.c.ID_CTR_STATUS.lc ()
            ).on ()
            .toOne (Account.class, "AS acct"
                , Account.c.ACCOUNTGUID.lc () + " AS accountguid"
            ).on ()
                
        );
        
        final Object uuid = r.get ("r.uuid");
        
        final List<Map<String, Object>> allCharges = db.getList (m
                
            .select (AnyChargeInfo.class, "AS root", "*")                
                
            .toMaybeOne (VocNsi50.class, "code", "guid").on ("root.code_vc_nsi_50=vc_nsi_50.code AND vc_nsi_50.isactual=1")
                
            .toMaybeOne (AdditionalService.class, "AS add_svc"
                , AdditionalService.c.CODE.lc ()
                , AdditionalService.c.GUID.lc ()
            ).on ()
                
            .toMaybeOne (MainMunicipalService.class, "AS mm_svc"
                , MainMunicipalService.c.CODE.lc ()
                , MainMunicipalService.c.GUID.lc ()
            ).on ()
                
            .toMaybeOne (GeneralNeedsMunicipalResource.class, "AS gen_res"
                , GeneralNeedsMunicipalResource.c.CODE.lc ()
                , GeneralNeedsMunicipalResource.c.GUID.lc ()
            ).on ()
                
            .toMaybeOne (VocNsi2.class, "code", "guid").on ("gen_res.code_vc_nsi_2=vc_nsi_2.code AND vc_nsi_2.isactual=1")
                
            .where (ChargeInfo.c.UUID_PAY_DOC, uuid)
            .where (ChargeInfo.c.TOTALPAYABLE.lc () + " >", 0)
            .where (EnTable.c.IS_DELETED, 0)

        );
        
        Map<UUID, BigDecimal> acct2total = new HashMap<> ();        
        allCharges.forEach ((t) -> {
            UUID        acct = (UUID)       t.get (ChargeInfo.c.UUID_BNK_ACCT.lc ());
            BigDecimal total = (BigDecimal) t.get (ChargeInfo.c.TOTALPAYABLE.lc  ());
            BigDecimal     v = acct2total.get (acct);
            if (v == null) v = BigDecimal.ZERO;
            acct2total.put (acct, v.add (total));
        });
        
        final List<Map<String, Object>> generalCharges = new ArrayList<> ();
        final List<Map<String, Object>> charges = new ArrayList<> ();
                
        for (Map<String, Object> i: allCharges) {
            
            i.put ("paymentinformationkey", i.get (ChargeInfo.c.UUID_BNK_ACCT.lc ()));
            
            switch (VocChargeInfoType.i.forId (i.get (ChargeInfo.c.ID_TYPE.lc ()))) {
                case GENERAL:
                    generalCharges.add (i);
                    break;
                case HOUSING:
                    i.put (ChargeInfo.__GENERAL, generalCharges);
                default:
                    charges.add (i);
            }
            
        }        
                
        r.put (ChargeInfo.TABLE_NAME, charges);        
        
        r.put (__ACCT2TOTAL, acct2total);

        r.put (BankAccount.TABLE_NAME, db.getList (m
            .select (BankAccount.class, "*")
            .where (EnTable.c.UUID.lc () + " IN", acct2total.keySet ().toArray ())
        ));
        
        r.put (PenaltiesAndCourtCosts.TABLE_NAME, db.getList (m
            .select (PenaltiesAndCourtCosts.class, "AS root", "*")
            .toOne (VocNsi329.class, "code", "guid").on ("root.code_vc_nsi_329=vc_nsi_329.code AND vc_nsi_329.isactual=1")
            .where (PenaltiesAndCourtCosts.c.UUID_PAY_DOC, uuid)
            .where (EnTable.c.IS_DELETED, 0)
        ));
        
        r.put (ComponentsOfCost.TABLE_NAME, db.getList (m
            .select (ComponentsOfCost.class, "AS root", "*")
            .where (ComponentsOfCost.c.UUID_PAY_DOC, uuid)
            .toOne (VocNsi331.class, "code", "guid").on ("root.code_vc_nsi_331=vc_nsi_331.code AND vc_nsi_331.isactual=1")
            .where (EnTable.c.IS_DELETED, 0)
            .where (ComponentsOfCost.c.COST.lc () + " >", 0)
        ));
        
        return r;
        
    }

    public static ImportPaymentDocumentRequest toImportPaymentDocumentRequest (Map<String, Object> r) {
        
        final ImportPaymentDocumentRequest result = DB.to.javaBean (ImportPaymentDocumentRequest.class, r);
        
        result.setConfirmAmountsCorrect (Boolean.TRUE);
        
        addPaymentInformation (result.getPaymentInformation (), (List <Map <String, Object>>) r.get (BankAccount.TABLE_NAME));
        
        result.getPaymentDocument ().add (toPaymentDocument (r));
        
        return result;
        
    }
    
    private static void addPaymentInformation (List<ImportPaymentDocumentRequest.PaymentInformation> paymentInformation, List<Map<String, Object>> list) {
        list.forEach ((t) -> paymentInformation.add (BankAccount.toPaymentInformation (t)));
    }
    
    private static void addPenaltiesAndCourtCosts (List<PaymentDocumentType.PenaltiesAndCourtCosts> penaltiesAndCourtCosts, List<Map<String, Object>> list) {
        list.forEach ((t) -> penaltiesAndCourtCosts.add (PenaltiesAndCourtCosts.toPenaltiesAndCourtCosts (t)));
    }    
    
    private static void addComponentsOfCost (List<PaymentDocumentType.ComponentsOfCost> сomponentsOfCost, List<Map<String, Object>> list) {
        list.forEach ((t) -> сomponentsOfCost.add (ComponentsOfCost.toComponentsOfCost (t)));
    }    
    
    private static void addChargeInfo (List<PaymentDocumentType.ChargeInfo> chargeInfo, List<Map<String, Object>> list) {
        list.forEach ((t) -> chargeInfo.add (ChargeInfo.toChargeInfo (t)));
    }    
    
    private static ImportPaymentDocumentRequest.PaymentDocument toPaymentDocument (Map<String, Object> r) {
        
        final ImportPaymentDocumentRequest.PaymentDocument result = DB.to.javaBean (ImportPaymentDocumentRequest.PaymentDocument.class, r);
        
        result.setTransportGUID (r.get ("uuid").toString ());
        
        final List<ImportPaymentDocumentRequest.PaymentDocument.DetailsPaymentInformation> detailsPaymentInformation = result.getDetailsPaymentInformation ();        
        addDetailsPaymentInformation (detailsPaymentInformation, (Map<UUID, BigDecimal>) r.get (__ACCT2TOTAL));
        if (detailsPaymentInformation.size () == 1) {
            result.setPaymentInformationKey (detailsPaymentInformation.get (0).getPaymentInformationKey ());
            detailsPaymentInformation.clear ();
        }
        
        addChargeInfo             (result.getChargeInfo (),             (List <Map <String, Object>>) r.get (ChargeInfo.TABLE_NAME));
        addPenaltiesAndCourtCosts (result.getPenaltiesAndCourtCosts (), (List <Map <String, Object>>) r.get (PenaltiesAndCourtCosts.TABLE_NAME));
        addComponentsOfCost       (result.getComponentsOfCost (),       (List <Map <String, Object>>) r.get (ComponentsOfCost.TABLE_NAME));
        
        return result;
        
    }
    
    private static void addDetailsPaymentInformation (List<ImportPaymentDocumentRequest.PaymentDocument.DetailsPaymentInformation> detailsPaymentInformation, Map<UUID, BigDecimal> acct2total) {        
        acct2total.forEach ((uuid, total) -> {            
            final ImportPaymentDocumentRequest.PaymentDocument.DetailsPaymentInformation i = new ImportPaymentDocumentRequest.PaymentDocument.DetailsPaymentInformation ();
            i.setPaymentInformationKey (uuid.toString ());
            i.setTotalPayableByPaymentInformation (total);
            detailsPaymentInformation.add (i);            
        });
    }

}