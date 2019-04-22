package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.gosuslugi.dom.schema.integration.bills.ImportAcknowledgmentRequest;

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
            ).on ()
                
//            .toOne (PaymentDocument.class
//            ).on ()
                
            .toOne (Payment.class
                , Payment.c.ORDERGUID.lc ()
            ).on ()
                
        );
        
        r.put ("notificationsoforderexecutionguid", r.get (Payment.c.ORDERGUID.lc ()));
        
        return r;
                
    }
        
    public static ImportAcknowledgmentRequest toImportAcknowledgment (Map<String, Object> r) {
        final ImportAcknowledgmentRequest result = DB.to.javaBean (ImportAcknowledgmentRequest.class, r);
        result.getAcknowledgmentRequestInfo ().add (to (r));
        return result;
    }    
    
    private static ImportAcknowledgmentRequest.AcknowledgmentRequestInfo to (Map<String, Object> r) {
        final ImportAcknowledgmentRequest.AcknowledgmentRequestInfo result = DB.to.javaBean (ImportAcknowledgmentRequest.AcknowledgmentRequestInfo.class, r);
        return result;
    }

}