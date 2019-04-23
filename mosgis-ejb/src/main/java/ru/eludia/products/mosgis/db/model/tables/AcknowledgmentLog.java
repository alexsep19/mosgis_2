package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.List;
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
              
        );
        
        Object uuid = r.get ("r.uuid");
        
        r.put ("items", 
                
            db.getList (
                    
                m
                .select (AcknowledgmentItem.class, "AS items", "*")
                .where  (AcknowledgmentItem.c.UUID_ACK, uuid)
                .where  (EnTable.c.IS_DELETED, 0)
                        
                .toOne (Acknowledgment.class, "AS ack"
                ).on ()
                    
                .toOne (PaymentDocument.class
                    , PaymentDocument.c.PAYMENTDOCUMENTID.lc () + " AS paymentdocumentid"
                    , PaymentDocument.c.YEAR.lc () + " AS year"
                    , PaymentDocument.c.MONTH.lc () + " AS month"
                ).on ()

                .toOne (Payment.class
                    , Payment.c.ORDERGUID.lc () + " AS notif_guid"
                ).on ()
                    
            )
                
        );

        return r;
                
    }
        
    public static ImportAcknowledgmentRequest toImportAcknowledgment (Map<String, Object> r) {
        final ImportAcknowledgmentRequest result = DB.to.javaBean (ImportAcknowledgmentRequest.class, r);        
        addAcknowledgmentRequestInfo (result.getAcknowledgmentRequestInfo (), (List <Map <String, Object>>) r.get ("items"));
        return result;
    }    
    
    private static void addAcknowledgmentRequestInfo (List<ImportAcknowledgmentRequest.AcknowledgmentRequestInfo> acknowledgmentRequestInfo, List<Map<String, Object>> list) {
        list.forEach ((t) -> acknowledgmentRequestInfo.add (toAcknowledgmentRequestInfo (t)));
    }
    
    private static ImportAcknowledgmentRequest.AcknowledgmentRequestInfo toAcknowledgmentRequestInfo (Map<String, Object> r) {
        r.put ("notificationsoforderexecutionguid", r.get ("notif_guid"));
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