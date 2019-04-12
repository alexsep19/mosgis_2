package ru.eludia.products.mosgis.jms.xl;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import ru.eludia.base.DB;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.xl.lines.InXlPayment;
import ru.eludia.products.mosgis.db.model.tables.Payment;
import ru.eludia.products.mosgis.jms.xl.base.XLMDB;
import ru.eludia.products.mosgis.jms.xl.base.XLException;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inXlPaymentsQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ParsePaymentsMDB extends XLMDB {
       
    private static final int N_COL_ERR  = 6;

    protected void addPayments (XSSFSheet sheet, UUID parent, DB db) throws SQLException {

        for (int i = 1; i <= sheet.getLastRowNum (); i ++) {
            
            final XSSFRow row = sheet.getRow (i);

            if (row.getCell (0) == null) continue;
            
            UUID uuid = (UUID) db.insertId (InXlPayment.class, InXlPayment.toHash (parent, i, row));

            try {
                
                db.update (InXlPayment.class, DB.HASH (
                    EnTable.c.UUID, uuid,
                    EnTable.c.IS_DELETED, 0
                ));
                
                db.update (Payment.class, DB.HASH (
                    EnTable.c.UUID, uuid,
                    EnTable.c.IS_DELETED, 0
                ));
            }
            catch (SQLException e) {

                String s = getErrorMessage (e);

                db.update (InXlPayment.class, DB.HASH (
                    EnTable.c.UUID,           uuid,
                    InXlPayment.c.ERR, s
                ));

                setCellStringValue (row, N_COL_ERR, s);
                
            }
            
        }
        
    }   

    private boolean checkPaymentLines (XSSFSheet sheet, DB db, UUID parent) throws SQLException {
        
        List<Map<String, Object>> brokenLines = db.getList (db.getModel ()
            .select (InXlPayment.class, "*")
            .where (InXlPayment.c.UUID_XL, parent)
            .where (EnTable.c.IS_DELETED, 1)
        );
        
        if (brokenLines.isEmpty ()) return true;
        
        for (Map<String, Object> brokenLine: brokenLines) {
            XSSFRow row = sheet.getRow ((int) DB.to.Long (brokenLine.get (InXlPayment.c.ORD.lc ())));
            XSSFCell cell = row.getLastCellNum () < N_COL_ERR ? row.createCell (N_COL_ERR) : row.getCell (N_COL_ERR);
            cell.setCellValue (brokenLine.get (InXlPayment.c.ERR.lc ()).toString ());
        }

        return false;
        
    }

    @Override
    protected void completeOK (DB db, UUID parent, XSSFWorkbook wb) throws SQLException {
        
        super.completeOK (db, parent, wb);
        
        db.update (Payment.class, DB.HASH (
            Payment.c.UUID_XL, parent,
            EnTable.c.IS_DELETED, 0
        ), Payment.c.UUID_XL.lc ());

    }

    @Override
    protected void completeFail (DB db, UUID parent, XSSFWorkbook wb) throws SQLException {
        
        super.completeFail (db, parent, wb);
        
        db.delete (db.getModel ()
            .select (Payment.class)
            .where  (Payment.c.UUID_XL, parent)
        );            
        
    }

    @Override
    protected void processLines (XSSFWorkbook wb, UUID uuid, DB db) throws Exception {
        
        boolean isOk = true;        
        
        final XSSFSheet sheetPayments = wb.getSheet ("Основные сведения");
        addPayments (sheetPayments, uuid, db);
        
        if (!checkPaymentLines (sheetPayments, db, uuid)) isOk = false;

        if (!isOk) throw new XLException ();

    }

}
