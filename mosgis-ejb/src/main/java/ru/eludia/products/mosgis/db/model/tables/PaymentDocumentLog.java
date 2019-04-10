package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.gosuslugi.dom.schema.integration.bills.ImportPaymentDocumentRequest;

public class PaymentDocumentLog extends GisWsLogTable {

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
        );
        
        final Object uuid = r.get ("r.uuid");
        
        final List<Map<String, Object>> charges = db.getList (m
            .select (ChargeInfo.class, "*")
            .where (ChargeInfo.c.UUID_PAY_DOC, uuid)
            .where (EnTable.c.IS_DELETED, 0)
        );

        r.put (ChargeInfo.TABLE_NAME, charges);

        Set <Object> uuidBnkAccts = charges.stream ()
            .map ((t) -> t.get (ChargeInfo.c.UUID_BNK_ACCT.lc ()))
            .collect (Collectors.toSet ());

        r.put (BankAccount.TABLE_NAME, db.getList (m
            .select (BankAccount.class, "*")
            .where (EnTable.c.UUID, uuidBnkAccts.toArray ())
        ));
        
        r.put (PenaltiesAndCourtCosts.TABLE_NAME, db.getList (m
            .select (PenaltiesAndCourtCosts.class, "*")
            .where (PenaltiesAndCourtCosts.c.UUID_PAY_DOC, uuid)
            .where (EnTable.c.IS_DELETED, 0)
        ));
        
        r.put (ComponentsOfCost.TABLE_NAME, db.getList (m
            .select (ComponentsOfCost.class, "*")
            .where (ComponentsOfCost.c.UUID_PAY_DOC, uuid)
            .where (EnTable.c.IS_DELETED, 0)
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
    
    private static ImportPaymentDocumentRequest.PaymentDocument toPaymentDocument (Map<String, Object> r) {
        final ImportPaymentDocumentRequest.PaymentDocument result = DB.to.javaBean (ImportPaymentDocumentRequest.PaymentDocument.class, r);
        return result;
    }

}