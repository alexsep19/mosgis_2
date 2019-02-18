package ru.eludia.products.mosgis.jms.xl;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.xl.lines.InXlHouse;
import ru.eludia.products.mosgis.db.model.tables.House;
import ru.eludia.products.mosgis.jms.xl.base.XLException;
import ru.eludia.products.mosgis.jms.xl.base.XLMDB;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inXlHousesQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ParseHousesMDB extends XLMDB {
    
    protected void processLines (XSSFWorkbook wb, UUID uuid, DB db) throws Exception {
        
        boolean isOk = true;
        
        final XSSFSheet sheetHouses = wb.getSheetAt (0);
        final XSSFSheet sheetHousesInfo = wb.getSheetAt (1);
        final XSSFSheet sheetBlocks = wb.getSheetAt (2);
        final XSSFSheet sheetBlocksInfo = wb.getSheetAt (3);
        final XSSFSheet sheetRooms = wb.getSheetAt (4);
        final XSSFSheet sheetRoomsInfo = wb.getSheetAt (5);
        
        addHousesLines (sheetHouses, uuid, db);
        if (!checkHousesLines (sheetHouses, db, uuid)) isOk = false;
        
        if (!isOk) throw new XLException ();
        
    }

    private void addHousesLines(XSSFSheet sheet, UUID parent, DB db) throws SQLException {
        
        for (int i = 2; i <= sheet.getLastRowNum (); i ++) {
            
            UUID uuid = (UUID) db.insertId (InXlHouse.class, InXlHouse.toHash (parent, i, sheet.getRow (i)));
            
            try {
                
                db.update (InXlHouse.class, DB.HASH (
                    EnTable.c.UUID, uuid,
                    EnTable.c.IS_DELETED, 0
                ));
                
            }
            catch (SQLException e) {

                String s = e.getMessage ();

                if (e.getErrorCode () == 20000) s =
                    new StringTokenizer (e.getMessage (), "\n\r")
                    .nextToken ()
                    .replace ("ORA-20000: ", "");

                db.update (InXlHouse.class, DB.HASH (
                    EnTable.c.UUID, uuid,
                    InXlHouse.c.ERR, s
                ));
                
            }
            
        }
        
    }

    private boolean checkHousesLines(XSSFSheet sheet, DB db, UUID parent) throws SQLException {
        
        List<Map<String, Object>> brokenLines = db.getList (db.getModel ()
            .select (InXlHouse.class, "*")
            .where (InXlHouse.c.UUID_XL, parent)
            .where (EnTable.c.IS_DELETED, 1)
        );
        
        if (brokenLines.isEmpty ()) return true;            
        
        for (Map<String, Object> brokenLine: brokenLines) {
            XSSFRow row = sheet.getRow ((int) DB.to.Long (brokenLine.get (InXlHouse.c.ORD.lc ())));
            XSSFCell cell = row.getLastCellNum () < 9 ? row.createCell (9) : row.getCell (9);
            cell.setCellValue (brokenLine.get (InXlHouse.c.ERR.lc ()).toString ());
        }

        return false;
        
    }
    
    @Override
    protected void completeOK (DB db, UUID parent) throws SQLException {
        
        super.completeOK (db, parent);
        
        db.update (House.class, DB.HASH (
            InXlHouse.c.UUID_XL, parent
        ), InXlHouse.c.UUID_XL.lc ());
        
    }

    @Override
    protected void completeFail (DB db, UUID parent, XSSFWorkbook wb) throws SQLException {
        
        super.completeFail (db, parent, wb);
        
        db.delete (db.getModel ()
            .select (House.class, "uuid")
            .where (InXlHouse.c.UUID_XL, parent)
        );       

    }
    
}
