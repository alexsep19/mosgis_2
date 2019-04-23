package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.gosuslugi.dom.schema.integration.bills.ImportAcknowledgmentRequest;
import ru.gosuslugi.dom.schema.integration.payments_base.AcknowledgmentRequestInfoType;

public class AcknowledgmentLog extends GisWsLogTable {

    public AcknowledgmentLog () {

        super (Acknowledgment.TABLE_NAME + "__log", "История редактирования записей квитирования", Acknowledgment.class
            , EnTable.c.class
            , Acknowledgment.c.class
        );

    }
    
    public static Map<String, Object> getForExport (DB db, String id) throws SQLException {
        
        final Model m = db.getModel ();
        
        final Map<String, Object> r = db.getMap (m                
                
            .get (AcknowledgmentLog.class, id)
                
            .toOne (Acknowledgment.class, "AS r"
                , EnTable.c.UUID.lc ()
            ).on ()
                
            .toOne (PaymentDocument.class
                , PaymentDocument.c.PAYMENTDOCUMENTID.lc () + " AS paymentdocumentid"
                , PaymentDocument.c.YEAR.lc () + " AS year"
                , PaymentDocument.c.MONTH.lc () + " AS month"
            ).on ()
                
            .toOne (Payment.class
                , Payment.c.ORDERGUID.lc () + " AS notif_guid"
            ).on ()
                
        );

        r.put ("notificationsoforderexecutionguid", r.get ("notif_guid"));

        return r;
                
    }
        
    public static ImportAcknowledgmentRequest toImportAcknowledgment (Map<String, Object> r) {
        final ImportAcknowledgmentRequest result = DB.to.javaBean (ImportAcknowledgmentRequest.class, r);
        result.getAcknowledgmentRequestInfo ().add (toAcknowledgmentRequestInfo (r));
        return result;
    }    
    
    private static ImportAcknowledgmentRequest.AcknowledgmentRequestInfo toAcknowledgmentRequestInfo (Map<String, Object> r) {
        final ImportAcknowledgmentRequest.AcknowledgmentRequestInfo result = DB.to.javaBean (ImportAcknowledgmentRequest.AcknowledgmentRequestInfo.class, r);
        result.setPaymentDocumentAck (toPaymentDocumentAck (r));
        result.setTransportGUID (UUID.randomUUID ().toString ());
        return result;
    }
    
    private static AcknowledgmentRequestInfoType.PaymentDocumentAck toPaymentDocumentAck (Map<String, Object> r) {
        final AcknowledgmentRequestInfoType.PaymentDocumentAck result = DB.to.javaBean (AcknowledgmentRequestInfoType.PaymentDocumentAck.class, r);        
        return result;
    }

}