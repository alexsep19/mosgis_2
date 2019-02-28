package ru.eludia.products.mosgis.jms.xl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.incoming.xl.lines.InXlBlock;
import ru.eludia.products.mosgis.db.model.incoming.xl.lines.InXlBlockInfo;
import ru.eludia.products.mosgis.db.model.incoming.xl.lines.InXlHouse;
import ru.eludia.products.mosgis.db.model.incoming.xl.lines.InXlHouseInfo;
import ru.eludia.products.mosgis.jms.xl.base.XLException;
import ru.eludia.products.mosgis.jms.xl.base.XLMDB;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inXlHousesQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ParseHousesMDB extends XLMDB {
    
    private final int HOUSE_CELL_COUNT = 12;
    private final int HOUSE_INFO_CELL_COUNT = 3;
    
    private final int BLOCK_CELL_COUNT = 8;
    private final int BLOCK_INFO_CELL_COUNT = 4;
    
    protected void processLines (XSSFWorkbook wb, UUID uuid, DB db) throws Exception {
        
        final XSSFSheet sheetHouses = wb.getSheetAt (0);
        final XSSFSheet sheetHousesInfo = wb.getSheetAt (1);
        final XSSFSheet sheetBlocks = wb.getSheetAt (2);
        final XSSFSheet sheetBlocksInfo = wb.getSheetAt (3);
        final XSSFSheet sheetRooms = wb.getSheetAt (4);
        final XSSFSheet sheetRoomsInfo = wb.getSheetAt (5);
        
        List <Map <String, Object>> houses = processHouses (sheetHouses, sheetHousesInfo, uuid, db);
        List <Map <String, Object>> blocks = processBlocks (sheetBlocks, sheetBlocksInfo, houses, uuid, db);
        
        db.update (InXlHouse.class, houses);
        db.update (InXlBlock.class, blocks);
        
        //addHousesLines (sheetHouses, uuid, db);
        //if (!checkHousesLines (sheetHouses, db, uuid)) isOk = false;
        
        //addHousesInfoLines (sheetHousesInfo, uuid, db);
        //if (!checkHousesInfoLines (sheetHousesInfo, db, uuid)) isOk = false;
        
        //if (!isOk) throw new XLException ();
        
    }
    
    private List <Map <String, Object>> processHouses (XSSFSheet sheetHouses, XSSFSheet sheetHousesInfo, UUID uuid, DB db) throws SQLException, XLException {
        
        boolean isOk = true;
        
        List <String> housesUnomList = new ArrayList <> ();
        List <String> housesAddressList = new ArrayList <> ();
        List <Map <String, Object>> housesList = new ArrayList <> ();
        for (int i = 2; i <= sheetHouses.getLastRowNum (); i++) {
            XSSFRow row = sheetHouses.getRow (i);
            Map<String, Object> record = InXlHouse.toHash(uuid, i, row);
            logger.info ("<HOUSE IMPORT> ROW: " + record.toString ());
            
            if (record.containsKey("err")) {
                XSSFCell errCell = row.getLastCellNum () <= HOUSE_CELL_COUNT ? row.createCell (HOUSE_CELL_COUNT) : row.getCell (HOUSE_CELL_COUNT);
                errCell.setCellValue (record.get ("err").toString ());
                isOk = false;
            }
            
            if (housesUnomList.contains(record.get ("unom").toString ())) {
                record.put ("err", "Документ уже содержит ЖД с данным UNOM");
                XSSFCell errCell = row.getLastCellNum () <= HOUSE_CELL_COUNT ? row.createCell (HOUSE_CELL_COUNT) : row.getCell (HOUSE_CELL_COUNT);
                errCell.setCellValue (record.get ("err").toString ());
                isOk = false;                
            }
            
            record.put ("uuid", db.insertId (InXlHouse.class, record));
            
            record.put ("is_deleted", 0);
            housesUnomList.add (record.get ("unom").toString ());
            housesAddressList.add (record.get ("address").toString ());
            housesList.add (record);
            
            if (!isOk) throw new XLException ();
            
        }
        
        List <Map <String, Object>> housesInfoList = new ArrayList <> ();
        for (int i = 2; i <= sheetHousesInfo.getLastRowNum (); i++) {
            XSSFRow row = sheetHousesInfo.getRow (i);
            Map<String, Object> record = InXlHouseInfo.toHash(uuid, i, row);
            logger.info ("<HOUSE INFO IMPORT> ROW: " + record.toString ());
            
            if (record.containsKey("err")) {
                XSSFCell errCell = row.getLastCellNum () <= HOUSE_INFO_CELL_COUNT ? row.createCell (HOUSE_INFO_CELL_COUNT) : row.getCell (HOUSE_INFO_CELL_COUNT);
                errCell.setCellValue (record.get ("err").toString ());
                isOk = false;
            }
            
            if (!housesAddressList.contains (record.get ("address").toString ())) {
                record.put ("err", "Документ не содержит информации о ЖД с заданным адресом");
                XSSFCell errCell = row.getLastCellNum () <= HOUSE_INFO_CELL_COUNT ? row.createCell (HOUSE_INFO_CELL_COUNT) : row.getCell (HOUSE_INFO_CELL_COUNT);
                errCell.setCellValue (record.get ("err").toString ());
                isOk = false;
            }
            
            record.put ("uuid", db.insertId (InXlHouseInfo.class, record));
            
            record.put ("is_deleted", 0);
            housesInfoList.add (record);
            
            if (!isOk) throw new XLException ();
            
        }
        
        for (Map <String, Object> house: housesList)
            for (Map <String, Object> houseInfo: housesInfoList)
                if (house.get ("address").equals (houseInfo.get ("address")))
                    if (houseInfo.containsKey (InXlHouseInfo.c.RESIDENTSCOUNT.lc ()))
                        house.put (InXlHouseInfo.c.RESIDENTSCOUNT.lc (), 
                                   houseInfo.get (InXlHouseInfo.c.RESIDENTSCOUNT.lc ()));
                    else
                        house.put (InXlHouseInfo.c.HASUNDERGROUNDPARKING.lc (),
                                   houseInfo.get (InXlHouseInfo.c.HASUNDERGROUNDPARKING.lc ()));
        
        return housesList;
        
    }

    private List<Map<String, Object>> processBlocks(XSSFSheet sheetBlocks, XSSFSheet sheetBlocksInfo, List <Map <String, Object>> houses, UUID uuid, DB db) throws SQLException, XLException {
        
        boolean isOk = true;
        
        List <Map <String, Object>> blocksList = new ArrayList <> ();
        for (int i = 2; i <= sheetBlocks.getLastRowNum () ; i++) {
            XSSFRow row = sheetBlocks.getRow (i);
            Map<String, Object> record = InXlBlock.toHash(uuid, i, row);
            logger.info ("<BLOCK IMPORT> ROW: " + record.toString ());
            
            if (record.containsKey("err")) {
                XSSFCell errCell = row.getLastCellNum () <= BLOCK_CELL_COUNT ? row.createCell (BLOCK_CELL_COUNT) : row.getCell (BLOCK_CELL_COUNT);
                errCell.setCellValue (record.get ("err").toString ());
                isOk = false;
            }
            
            final Optional <Map <String, Object>> houseMap = houses.stream ()
                                                                   .filter ((map) -> record.get ("address").toString ().equals (map.get ("address").toString ()))
                                                                   .findFirst ();
            if (!houseMap.isPresent ()) {
                record.put ("err", "Документ не содержит информации о ЖД с заданным адресом");
                XSSFCell errCell = row.getLastCellNum () <= BLOCK_CELL_COUNT ? row.createCell (BLOCK_CELL_COUNT) : row.getCell (BLOCK_CELL_COUNT);
                errCell.setCellValue (record.get ("err").toString ());
                isOk = false;
            }
            else if ((int) houseMap.get ().get ("hasblocks") != 1) {
                record.put ("err", "ЖД с заданным адресом не является домом блокированной застройки");
                XSSFCell errCell = row.getLastCellNum () <= BLOCK_CELL_COUNT ? row.createCell (BLOCK_CELL_COUNT) : row.getCell (BLOCK_CELL_COUNT);
                errCell.setCellValue (record.get ("err").toString ());
                isOk = false;
            }
            else record.put ("house_unom", houseMap.get (). get ("unom"));
            
            final long blockNumCount = blocksList.stream ()
                                                 .filter ((map) -> record.get ("address").equals (map.get ("address")) && 
                                                                   record.get ("blocknum").equals (map.get ("blocknum")))
                                                 .count ();
            if (blockNumCount != 0) {
                record.put ("err", "Документ уже содержит информации о блоке по заданному адресу и номеру");
                XSSFCell errCell = row.getLastCellNum () <= BLOCK_CELL_COUNT ? row.createCell (BLOCK_CELL_COUNT) : row.getCell (BLOCK_CELL_COUNT);
                errCell.setCellValue (record.get ("err").toString ());
                isOk = false;
            }
            
            record.put ("uuid", db.insertId (InXlBlock.class, record));
            
            record.put ("is_deleted", 0);
            blocksList.add (record);
            
            if (!isOk) throw new XLException ();
                                               
        }
        
        List <Map <String, Object>> blocksInfoList = new ArrayList <> ();
        for (int i = 2; i <= sheetBlocksInfo.getLastRowNum () ; i++) {
            XSSFRow row = sheetBlocksInfo.getRow (i);
            Map<String, Object> record = InXlBlockInfo.toHash(uuid, i, row);
            logger.info ("<BLOCK INFO IMPORT> ROW: " + record.toString ());
            
            if (record.containsKey("err")) {
                XSSFCell errCell = row.getLastCellNum () <= BLOCK_INFO_CELL_COUNT ? row.createCell (BLOCK_INFO_CELL_COUNT) : row.getCell (BLOCK_INFO_CELL_COUNT);
                errCell.setCellValue (record.get ("err").toString ());
                isOk = false;
            }
            
            final Optional <Map <String, Object>> blockMap = blocksList.stream ()
                                                                       .filter ((map) -> map.get ("address").equals (record.get ("address")) &&
                                                                                         map.get ("blocknum").equals (record.get ("blocknum")))
                                                                       .findFirst ();
            
            if (!blockMap.isPresent ()) {
                record.put ("err", "Документ не содержит информации о блоке с заданным адресом и номером");
                XSSFCell errCell = row.getLastCellNum () <= HOUSE_INFO_CELL_COUNT ? row.createCell (HOUSE_INFO_CELL_COUNT) : row.getCell (HOUSE_INFO_CELL_COUNT);
                errCell.setCellValue (record.get ("err").toString ());
                isOk = false;
            }
            
            record.put ("uuid", db.insertId (InXlBlockInfo.class, record));
            
            record.put ("is_deleted", 0);
            blocksInfoList.add (record);
            
            if (!isOk) throw new XLException ();
        }
        
        for (Map <String, Object> block: blocksList)
            for (Map <String, Object> blockInfo: blocksInfoList)
                if (block.get ("address").equals (blockInfo.get ("address")) && block.get ("blocknum").equals (blockInfo.get ("blocknum")))
                    if (blockInfo.containsKey (InXlBlockInfo.c.F_20002.lc ()))
                        block.put (InXlBlockInfo.c.F_20002.lc (), 
                                   blockInfo.get (InXlBlockInfo.c.F_20002.lc ()));
                    else if (blockInfo.containsKey (InXlBlockInfo.c.F_20003.lc ()))
                        block.put (InXlBlockInfo.c.F_20003.lc (),
                                   blockInfo.get (InXlBlockInfo.c.F_20003.lc ()));
                    else
                        block.put (InXlBlockInfo.c.F_20125.lc (),
                                   blockInfo.get (InXlBlockInfo.c.F_20125.lc ()));
        
        return blocksList;
        
    }
    
}
