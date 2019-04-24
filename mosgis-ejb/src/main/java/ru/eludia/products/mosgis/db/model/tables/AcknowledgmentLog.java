package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocBic;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi50;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi329;
import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi50;
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
                
            .get (AcknowledgmentLog.class, id, "*")
                
            .toOne (Acknowledgment.class, "AS r"
                , EnTable.c.UUID.lc ()
                , Acknowledgment.c.ID_CTR_STATUS.lc ()
            ).on ()
                
            .toOne (PaymentDocument.class).on ()
                
            .toMaybeOne (VocOrganization.class
                , VocOrganization.c.ORGPPAGUID.lc () + " AS orgppaguid"
            ).on ()            
              
        );
        
        Object uuid = r.get ("r.uuid");
        
        r.put ("nsi50", db.getIdx (m.select (Nsi50.class, "*")));
        
        r.put ("items", 
                
            db.getList (
                    
                m
                .select (AcknowledgmentItem.class, "AS root", "*")
                .where  (AcknowledgmentItem.c.UUID_ACK, uuid)
                .where  (EnTable.c.IS_DELETED, 0)
                        
                .toOne (Acknowledgment.class, "AS ack").on ()
                    
                .toOne (PaymentDocument.class
                    , PaymentDocument.c.PAYMENTDOCUMENTID.lc () + " AS paymentdocumentid"
                    , PaymentDocument.c.YEAR.lc () + " AS year"
                    , PaymentDocument.c.MONTH.lc () + " AS month"
                ).on ()

                .toOne (Payment.class
                    , Payment.c.ORDERGUID.lc () + " AS notif_guid"
                ).on ()

                .toMaybeOne (PenaltiesAndCourtCosts.class, "AS pen").on ()
                .toMaybeOne (VocNsi329.class, "code", "guid").on ("pen.code_vc_nsi_329=vc_nsi_329.code AND vc_nsi_329.isactual=1")

                .toMaybeOne (AnyChargeInfo.class, "AS chg", "*")
                    .where (ChargeInfo.c.UUID_GEN_NEED_RES.lc () + " IS NULL")
                    .on ("chg.id=root." + AcknowledgmentItem.c.UUID_CHARGE.lc ()
                )
                .toMaybeOne (VocNsi50.class,             "guid AS hstype").on ("vc_nsi_50.code=(CASE WHEN chg.UUID_INS_PRODUCT IS NOT NULL THEN '11' ELSE chg.code_vc_nsi_50 END) AND vc_nsi_50.isactual=1")
                .toMaybeOne (AdditionalService.class,    "guid AS astype").on ()
                .toMaybeOne (MainMunicipalService.class, "guid AS mstype").on ()
                .toMaybeOne (BankAccount.class, "AS ba"
                    , BankAccount.c.ACCOUNTNUMBER.lc () + " AS operatingaccountnumber"
                ).on ()
                .toMaybeOne (VocBic.class
                    , VocBic.c.BIC.lc () + " AS bankbik"
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
    
    private static String getNN (Map<String, Object> r, String key) {
        Object v = r.get (key);
        return v == null ? null : v.toString ();
    }
    
    private static AcknowledgmentRequestInfoType.PaymentDocumentAck toPaymentDocumentAck (Map<String, Object> r) {
        
        final AcknowledgmentRequestInfoType.PaymentDocumentAck result = DB.to.javaBean (AcknowledgmentRequestInfoType.PaymentDocumentAck.class, r);        
        
        if (DB.ok (r.get ("uuid_penalty"))) {
            result.setPServiceType (NsiTable.toDom (r, "vc_nsi_329"));
        }
        else {
            result.setHSType (getNN (r, "hstype"));
            result.setASType (getNN (r, "astype"));            
            result.setMSType (getNN (r, "mstype"));            
        }
        
        result.setPaymentInformation (toPaymentInformation (r));
        
        return result;
        
    }
    
    private static AcknowledgmentRequestInfoType.PaymentDocumentAck.PaymentInformation toPaymentInformation (Map<String, Object> r) {
        final AcknowledgmentRequestInfoType.PaymentDocumentAck.PaymentInformation result = DB.to.javaBean (AcknowledgmentRequestInfoType.PaymentDocumentAck.PaymentInformation.class, r);
        result.setPaymentInformationGuid (null);
        return result;
    }

}