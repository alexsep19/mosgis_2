package ru.eludia.products.mosgis.jms.xl;

import java.util.UUID;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.eludia.base.DB;
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

    private void addHousesLines(XSSFSheet sheetHouses, UUID uuid, DB db) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private boolean checkHousesLines(XSSFSheet sheetHouses, DB db, UUID uuid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
