package ru.eludia.products.mosgis.jms.xl;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.logging.Logger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import ru.eludia.base.DB;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.Model;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.xl.lines.InXlSupplyResourceContract;
import ru.eludia.products.mosgis.db.model.incoming.xl.lines.InXlSupplyResourceContractObject;
import ru.eludia.products.mosgis.db.model.incoming.xl.lines.InXlSupplyResourceContractOtherQualityLevel;
import ru.eludia.products.mosgis.db.model.incoming.xl.lines.InXlSupplyResourceContractService;
import ru.eludia.products.mosgis.db.model.incoming.xl.lines.InXlSupplyResourceContractSubject;
import ru.eludia.products.mosgis.db.model.incoming.xl.lines.InXlSupplyResourceContractQualityLevel;
import ru.eludia.products.mosgis.db.model.incoming.xl.lines.InXlSupplyResourceContractTemperatureChart;
import ru.eludia.products.mosgis.db.model.incoming.xl.lines.InXlSupplyResourceContractVolume;
import ru.eludia.products.mosgis.db.model.tables.Block;
import ru.eludia.products.mosgis.db.model.tables.ResidentialPremise;
import ru.eludia.products.mosgis.db.model.tables.NonResidentialPremise;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractLog;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractObject;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractObjectLog;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractOtherQualityLevel;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractQualityLevel;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractSubject;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractSubjectLog;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractTemperatureChart;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.eludia.products.mosgis.db.model.voc.VocPersonLog;
import ru.eludia.products.mosgis.jms.xl.base.XLMDB;
import ru.eludia.products.mosgis.jms.xl.base.XLException;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "mosgis.inXlSupplyResourceContractsQueue")
    , @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
    , @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ParseSupplyResourceContractsMDB extends XLMDB {

    private static final Logger logger = Logger.getLogger(ParseSupplyResourceContractsMDB.class.getName());

    protected void addContractLines (XSSFSheet sheet, UUID parent, DB db, Map<String, Map<String, Object>> vocs) throws SQLException {

	for (int i = 2; i <= sheet.getLastRowNum (); i ++) {
            
            UUID uuid = (UUID) db.insertId (InXlSupplyResourceContract.class
		, InXlSupplyResourceContract.toHash (parent, i, sheet.getRow (i), vocs)
	    );
            
            try {
                
                db.update (InXlSupplyResourceContract.class, DB.HASH (
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

                db.update (InXlSupplyResourceContract.class, DB.HASH (
                    EnTable.c.UUID, uuid,
                    InXlSupplyResourceContract.c.ERR, s
                ));
                
            }
            
        }
    }

    private boolean checkContractLines (XSSFSheet sheet, DB db, UUID parent) throws SQLException {
        
        List<Map<String, Object>> brokenLines = db.getList (db.getModel ()
            .select (InXlSupplyResourceContract.class, "*")
            .where (SupplyResourceContract.c.UUID_XL, parent)
            .where (EnTable.c.IS_DELETED, 1)
        );
        
        if (brokenLines.isEmpty ()) return true;
        
        for (Map<String, Object> brokenLine: brokenLines) {
	    XSSFRow row = sheet.getRow ((int) DB.to.Long (brokenLine.get (InXlSupplyResourceContract.c.ORD.lc ())));
	    XSSFCell cell = row.getLastCellNum () < 43 ? row.createCell (43) : row.getCell (43);
	    String val = brokenLine.get(InXlSupplyResourceContract.c.ERR.lc()).toString();
	    cell.setCellValue (val);
        }

        return false;
    }

    protected void addSubjectLines(XSSFSheet sheet, UUID parent, DB db, Map<String, Map<String, Object>> vocs) throws SQLException {

	for (int i = 2; i <= sheet.getLastRowNum(); i++) {

	    UUID uuid = (UUID) db.insertId(InXlSupplyResourceContractSubject.class
		, InXlSupplyResourceContractSubject.toHash(parent, i, sheet.getRow(i), vocs)
	    );

	    try {

		db.update(InXlSupplyResourceContractSubject.class, DB.HASH(
		    EnTable.c.UUID, uuid,
		    EnTable.c.IS_DELETED, 0
		));

	    } catch (SQLException e) {

		String s = e.getMessage();

		if (e.getErrorCode() == 20000) {
		    s
			= new StringTokenizer(e.getMessage(), "\n\r")
			    .nextToken()
			    .replace("ORA-20000: ", "");
		}

		db.update(InXlSupplyResourceContractSubject.class, DB.HASH(
		    EnTable.c.UUID, uuid,
		    InXlSupplyResourceContractSubject.c.ERR, s
		));

	    }

	}
    }

    private boolean checkSubjectLines(XSSFSheet sheet, DB db, UUID parent) throws SQLException {

	List<Map<String, Object>> brokenLines = db.getList(db.getModel()
	    .select(InXlSupplyResourceContractSubject.class, "*")
	    .where(SupplyResourceContractSubject.c.UUID_XL, parent)
	    .where(EnTable.c.IS_DELETED, 1)
	);

	if (brokenLines.isEmpty()) {
	    return true;
	}

	for (Map<String, Object> brokenLine : brokenLines) {
	    XSSFRow row = sheet.getRow((int) DB.to.Long(brokenLine.get(InXlSupplyResourceContractSubject.c.ORD.lc())));
	    XSSFCell cell = row.getLastCellNum() < 10 ? row.createCell(10) : row.getCell(10);
	    String val = brokenLine.get(InXlSupplyResourceContractSubject.c.ERR.lc()).toString();
	    cell.setCellValue(val);
	}

	return false;
    }

    protected void addObjectLines(XSSFSheet sheet, UUID parent, DB db) throws SQLException {

	for (int i = 1; i <= sheet.getLastRowNum(); i++) {

	    UUID uuid = (UUID) db.insertId(InXlSupplyResourceContractObject.class, InXlSupplyResourceContractObject.toHash(parent, i, sheet.getRow(i)));

	    try {

		db.update(InXlSupplyResourceContractObject.class, DB.HASH(
		    EnTable.c.UUID, uuid,
		    EnTable.c.IS_DELETED, 0
		));

	    } catch (SQLException e) {

		String s = e.getMessage();

		if (e.getErrorCode() == 20000) {
		    s
			= new StringTokenizer(e.getMessage(), "\n\r")
			    .nextToken()
			    .replace("ORA-20000: ", "");
		}

		db.update(InXlSupplyResourceContractObject.class, DB.HASH(
		    EnTable.c.UUID, uuid,
		    InXlSupplyResourceContractObject.c.ERR, s
		));

	    }

	}
    }

    private boolean checkObjectLines(XSSFSheet sheet, DB db, UUID parent) throws SQLException {

	List<Map<String, Object>> brokenLines = db.getList(db.getModel()
	    .select(InXlSupplyResourceContractObject.class, "*")
	    .where(SupplyResourceContractObject.c.UUID_XL, parent)
	    .where(EnTable.c.IS_DELETED, 1)
	);

	if (brokenLines.isEmpty()) {
	    return true;
	}

	for (Map<String, Object> brokenLine : brokenLines) {
	    XSSFRow row = sheet.getRow((int) DB.to.Long(brokenLine.get(InXlSupplyResourceContractObject.c.ORD.lc())));
	    XSSFCell cell = row.getLastCellNum() < 13 ? row.createCell(13) : row.getCell(13);
	    String val = brokenLine.get(InXlSupplyResourceContractObject.c.ERR.lc()).toString();
	    cell.setCellValue(val);
	}

	return false;
    }

    protected void addServiceLines(XSSFSheet sheet, UUID parent, DB db, Map<String, Map<String, Object>> vocs) throws SQLException {

	for (int i = 2; i <= sheet.getLastRowNum(); i++) {

	    UUID uuid = (UUID) db.insertId(InXlSupplyResourceContractService.class
		, InXlSupplyResourceContractService.toHash(parent, i, sheet.getRow(i), vocs)
	    );

	    try {

		db.update(InXlSupplyResourceContractService.class, DB.HASH(
		    EnTable.c.UUID, uuid,
		    EnTable.c.IS_DELETED, 0
		));

	    } catch (SQLException e) {

		String s = e.getMessage();

		if (e.getErrorCode() == 20000) {
		    s
			= new StringTokenizer(e.getMessage(), "\n\r")
			    .nextToken()
			    .replace("ORA-20000: ", "");
		}

		db.update(InXlSupplyResourceContractService.class, DB.HASH(
		    EnTable.c.UUID, uuid,
		    InXlSupplyResourceContractService.c.ERR, s
		));

	    }

	}
    }

    private boolean checkServiceLines(XSSFSheet sheet, DB db, UUID parent) throws SQLException {

	List<Map<String, Object>> brokenLines = db.getList(db.getModel()
	    .select(InXlSupplyResourceContractService.class, "*")
	    .where(SupplyResourceContractSubject.c.UUID_XL, parent)
	    .where(EnTable.c.IS_DELETED, 1)
	);

	if (brokenLines.isEmpty()) {
	    return true;
	}

	for (Map<String, Object> brokenLine : brokenLines) {
	    XSSFRow row = sheet.getRow((int) DB.to.Long(brokenLine.get(InXlSupplyResourceContractService.c.ORD.lc())));
	    XSSFCell cell = row.getLastCellNum() < 12 ? row.createCell(12) : row.getCell(12);
	    String val = brokenLine.get(InXlSupplyResourceContractService.c.ERR.lc()).toString();
	    cell.setCellValue(val);
	}

	return false;
    }

    protected void addQualityLines(XSSFSheet sheet, UUID parent, DB db, Map<String, Map<String, Object>> vocs) throws SQLException {

	for (int i = 1; i <= sheet.getLastRowNum(); i++) {

	    UUID uuid = (UUID) db.insertId(InXlSupplyResourceContractQualityLevel.class,
		 InXlSupplyResourceContractQualityLevel.toHash(parent, i, sheet.getRow(i), vocs)
	    );

	    try {

		db.update(InXlSupplyResourceContractQualityLevel.class, DB.HASH(
		    EnTable.c.UUID, uuid,
		    EnTable.c.IS_DELETED, 0
		));

	    } catch (SQLException e) {

		String s = e.getMessage();

		if (e.getErrorCode() == 20000) {
		    s
			= new StringTokenizer(e.getMessage(), "\n\r")
			    .nextToken()
			    .replace("ORA-20000: ", "");
		}

		db.update(InXlSupplyResourceContractQualityLevel.class, DB.HASH(
		    EnTable.c.UUID, uuid,
		    InXlSupplyResourceContractQualityLevel.c.ERR, s
		));

	    }

	}
    }

    private boolean checkQualityLines(XSSFSheet sheet, DB db, UUID parent) throws SQLException {

	List<Map<String, Object>> brokenLines = db.getList(db.getModel()
	    .select(InXlSupplyResourceContractQualityLevel.class, "*")
	    .where(SupplyResourceContractQualityLevel.c.UUID_XL, parent)
	    .where(EnTable.c.IS_DELETED, 1)
	);

	if (brokenLines.isEmpty()) {
	    return true;
	}

	for (Map<String, Object> brokenLine : brokenLines) {
	    XSSFRow row = sheet.getRow((int) DB.to.Long(brokenLine.get(InXlSupplyResourceContractQualityLevel.c.ORD.lc())));
	    XSSFCell cell = row.getLastCellNum() < 15 ? row.createCell(15) : row.getCell(15);
	    String val = brokenLine.get(InXlSupplyResourceContractQualityLevel.c.ERR.lc()).toString();
	    cell.setCellValue(val);
	}

	return false;
    }

    protected void addOtherQualityLines(XSSFSheet sheet, UUID parent, DB db, Map<String, Map<String, Object>> vocs) throws SQLException {

	for (int i = 1; i <= sheet.getLastRowNum(); i++) {

	    UUID uuid = (UUID) db.insertId(InXlSupplyResourceContractOtherQualityLevel.class,
		InXlSupplyResourceContractOtherQualityLevel.toHash(parent, i, sheet.getRow(i), vocs)
	    );

	    try {

		db.update(InXlSupplyResourceContractOtherQualityLevel.class, DB.HASH(
		    EnTable.c.UUID, uuid,
		    EnTable.c.IS_DELETED, 0
		));

	    } catch (SQLException e) {

		String s = e.getMessage();

		if (e.getErrorCode() == 20000) {
		    s
			= new StringTokenizer(e.getMessage(), "\n\r")
			    .nextToken()
			    .replace("ORA-20000: ", "");
		}

		db.update(InXlSupplyResourceContractOtherQualityLevel.class, DB.HASH(
		    EnTable.c.UUID, uuid,
		    InXlSupplyResourceContractOtherQualityLevel.c.ERR, s
		));

	    }

	}
    }

    private boolean checkOtherQualityLines(XSSFSheet sheet, DB db, UUID parent) throws SQLException {

	List<Map<String, Object>> brokenLines = db.getList(db.getModel()
	    .select(InXlSupplyResourceContractOtherQualityLevel.class, "*")
	    .where(SupplyResourceContractOtherQualityLevel.c.UUID_XL, parent)
	    .where(EnTable.c.IS_DELETED, 1)
	);

	if (brokenLines.isEmpty()) {
	    return true;
	}

	for (Map<String, Object> brokenLine : brokenLines) {
	    XSSFRow row = sheet.getRow((int) DB.to.Long(brokenLine.get(InXlSupplyResourceContractOtherQualityLevel.c.ORD.lc())));
	    XSSFCell cell = row.getLastCellNum() < 16 ? row.createCell(16) : row.getCell(16);
	    String val = brokenLine.get(InXlSupplyResourceContractOtherQualityLevel.c.ERR.lc()).toString();
	    cell.setCellValue(val);
	}

	return false;
    }

    protected void addVolumeLines(XSSFSheet sheet, UUID parent, DB db, Map<String, Map<String, Object>> vocs) throws SQLException {

	for (int i = 2; i <= sheet.getLastRowNum(); i++) {

	    UUID uuid = (UUID) db.insertId(InXlSupplyResourceContractVolume.class,
		InXlSupplyResourceContractVolume.toHash(parent, i, sheet.getRow(i), vocs)
	    );

	    try {
		db.update(InXlSupplyResourceContractVolume.class, DB.HASH(
		    EnTable.c.UUID, uuid,
		    EnTable.c.IS_DELETED, 0
		));

	    } catch (SQLException e) {

		String s = e.getMessage();

		if (e.getErrorCode() == 20000) {
		    s
			= new StringTokenizer(e.getMessage(), "\n\r")
			    .nextToken()
			    .replace("ORA-20000: ", "");
		}

		db.update(InXlSupplyResourceContractVolume.class, DB.HASH(
		    EnTable.c.UUID, uuid,
		    InXlSupplyResourceContractVolume.c.ERR, s
		));

	    }

	}
    }

    private boolean checkVolumeLines(XSSFSheet sheet, DB db, UUID parent) throws SQLException {

	List<Map<String, Object>> brokenLines = db.getList(db.getModel()
	    .select(InXlSupplyResourceContractVolume.class, "*")
	    .where(SupplyResourceContractObject.c.UUID_XL, parent)
	    .where(EnTable.c.IS_DELETED, 1)
	);

	if (brokenLines.isEmpty()) {
	    return true;
	}

	for (Map<String, Object> brokenLine : brokenLines) {
	    XSSFRow row = sheet.getRow((int) DB.to.Long(brokenLine.get(InXlSupplyResourceContractVolume.c.ORD.lc())));
	    XSSFCell cell = row.getLastCellNum() < 10 ? row.createCell(10) : row.getCell(10);
	    String val = brokenLine.get(InXlSupplyResourceContractVolume.c.ERR.lc()).toString();
	    cell.setCellValue(val);
	}

	return false;
    }

    protected void addTemperatureChartLines(XSSFSheet sheet, UUID parent, DB db, Map<String, Map<String, Object>> vocs) throws SQLException {

	for (int i = 1; i <= sheet.getLastRowNum(); i++) {

	    UUID uuid = (UUID) db.insertId(InXlSupplyResourceContractTemperatureChart.class,
		InXlSupplyResourceContractTemperatureChart.toHash(parent, i, sheet.getRow(i), vocs)
	    );

	    try {
		db.update(InXlSupplyResourceContractTemperatureChart.class, DB.HASH(
		    EnTable.c.UUID, uuid,
		    EnTable.c.IS_DELETED, 0
		));

	    } catch (SQLException e) {

		String s = e.getMessage();

		if (e.getErrorCode() == 20000) {
		    s
			= new StringTokenizer(e.getMessage(), "\n\r")
			    .nextToken()
			    .replace("ORA-20000: ", "");
		}

		db.update(InXlSupplyResourceContractTemperatureChart.class, DB.HASH(
		    EnTable.c.UUID, uuid,
		    InXlSupplyResourceContractTemperatureChart.c.ERR, s
		));

	    }

	}
    }

    private boolean checkTemperatureChartLines(XSSFSheet sheet, DB db, UUID parent) throws SQLException {

	List<Map<String, Object>> brokenLines = db.getList(db.getModel()
	    .select(InXlSupplyResourceContractTemperatureChart.class, "*")
	    .where(SupplyResourceContractObject.c.UUID_XL, parent)
	    .where(EnTable.c.IS_DELETED, 1)
	);

	if (brokenLines.isEmpty()) {
	    return true;
	}

	for (Map<String, Object> brokenLine : brokenLines) {
	    XSSFRow row = sheet.getRow((int) DB.to.Long(brokenLine.get(InXlSupplyResourceContractTemperatureChart.c.ORD.lc())));
	    XSSFCell cell = row.getLastCellNum() < 8 ? row.createCell(8) : row.getCell(8);
	    String val = brokenLine.get(InXlSupplyResourceContractTemperatureChart.c.ERR.lc()).toString();
	    cell.setCellValue(val);
	}

	return false;
    }
    protected Map<String, Object> processVocNsi58Lines(XSSFSheet sheet) throws XLException {

	final Map<String, Object> r = DB.HASH();

	for (int i = 1; i <= sheet.getLastRowNum(); i++) {
	    XSSFRow row = sheet.getRow(i);
	    Object label = EnTable.toString(row, 0, "Не указано Название(столбец A)");
	    Object code = EnTable.toString(row, 1, "Не указан Код(столбец B)");
	    r.put(label.toString(), code.toString());
	}

	return r;
    }

    protected Map<String, Object> processIDLines(XSSFSheet sheet) throws XLException {

	final Map<String, Object> r = DB.HASH();

	for (int i = 0; i <= sheet.getLastRowNum(); i++) {
	    XSSFRow row = sheet.getRow(i);
	    Object label = EnTable.toString(row, 0, "Не указано Название(столбец A)");
	    Object code = EnTable.toString(row, 1, "Не указан Код(столбец B)");
	    code = code.toString().replace("\u00a0", " ").trim(); // nbsp;
	    r.put(label.toString(), code.toString());
	}

	return r;
    }

    protected Map<String, Object> processVocNsi3Lines(XSSFSheet sheet) throws XLException {

	final Map<String, Object> r = DB.HASH();

	for (int i = 0; i <= sheet.getLastRowNum(); i++) {
	    XSSFRow row = sheet.getRow(i);
	    Object label = EnTable.toString(row, 0, "Не указано Название(столбец A)");
	    Object code = EnTable.toString(row, 1, "Не указан Код(столбец B)");
	    r.put(label.toString(), code.toString());
	}

	return r;
    }

    protected Map<String, Object> processVocNsi236Lines(XSSFSheet sheet) throws XLException {

	final Map<String, Object> r = DB.HASH();

	for (int i = 1; i <= sheet.getLastRowNum(); i++) {
	    XSSFRow row = sheet.getRow(i);
	    Object label = EnTable.toString(row, 0, "Не указано Название(столбец A)");
	    Object code = EnTable.toString(row, 1, "Не указан Код(столбец B)");
	    r.put(label.toString(), code.toString());
	}

	return r;
    }

    protected Map<String, Object> processVocNsi276Lines(XSSFSheet sheet) throws XLException {

	final Map<String, Object> r = DB.HASH();

	for (int i = 1; i <= sheet.getLastRowNum(); i++) {
	    XSSFRow row = sheet.getRow(i);
	    Object label = EnTable.toString(row, 0, "Не указано Название(столбец A)");
	    Object code = EnTable.toString(row, 1, "Не указан Код(столбец B)");
	    r.put(label.toString(), code.toString());
	}

	return r;
    }

    @Override
    protected void completeOK (DB db, UUID parent) throws SQLException {

	super.completeOK (db, parent);

	db.update(VocPerson.class, DB.HASH(
	    VocPerson.c.UUID_XL, parent,
	    EnTable.c.IS_DELETED, 0
	), VocPerson.c.UUID_XL.lc());

        db.update (SupplyResourceContract.class, DB.HASH (
            SupplyResourceContract.c.UUID_XL, parent,
            EnTable.c.IS_DELETED, 0
        ), SupplyResourceContract.c.UUID_XL.lc ());


	db.update(ResidentialPremise.class, DB.HASH(
	    "uuid_xl", parent,
	    "is_deleted", 0
	), "uuid_xl");
	db.update(NonResidentialPremise.class, DB.HASH(
	    "uuid_xl", parent,
	    "is_deleted", 0
	), "uuid_xl");
	db.update(Block.class, DB.HASH(
	    Block.c.UUID_XL, parent,
	    EnTable.c.IS_DELETED, 0
	), Block.c.UUID_XL.lc());
	db.update(SupplyResourceContractObject.class, DB.HASH(
	    SupplyResourceContractObject.c.UUID_XL, parent,
	    EnTable.c.IS_DELETED, 0
	), SupplyResourceContractObject.c.UUID_XL.lc());

	db.update(SupplyResourceContractSubject.class, DB.HASH(
	    SupplyResourceContractSubject.c.UUID_XL, parent,
	    EnTable.c.IS_DELETED, 0
	), SupplyResourceContractSubject.c.UUID_XL.lc());


	db.update(SupplyResourceContractQualityLevel.class, DB.HASH(
	    SupplyResourceContractQualityLevel.c.UUID_XL, parent,
	    EnTable.c.IS_DELETED, 0
	), SupplyResourceContractQualityLevel.c.UUID_XL.lc());
	db.update(SupplyResourceContractOtherQualityLevel.class, DB.HASH(
	    SupplyResourceContractOtherQualityLevel.c.UUID_XL, parent,
	    EnTable.c.IS_DELETED, 0
	), SupplyResourceContractOtherQualityLevel.c.UUID_XL.lc());
	db.update(SupplyResourceContractTemperatureChart.class, DB.HASH(
	    SupplyResourceContractTemperatureChart.c.UUID_XL, parent,
	    EnTable.c.IS_DELETED, 0
	), SupplyResourceContractTemperatureChart.c.UUID_XL.lc());
    }

    @Override
    protected void completeFail (DB db, UUID parent, XSSFWorkbook wb) throws SQLException {

        super.completeFail (db, parent, wb);

	killXlSupplyResourceContract(db, parent);
    }

    protected void killXlSupplyResourceContract(DB db, UUID parent) throws SQLException {

	final Model m = db.getModel();

	db.delete(m
	    .select(SupplyResourceContractQualityLevel.class, "uuid")
	    .where(SupplyResourceContractQualityLevel.c.UUID_XL, parent)
	);
	db.delete(m
	    .select(SupplyResourceContractOtherQualityLevel.class, "uuid")
	    .where(SupplyResourceContractOtherQualityLevel.c.UUID_XL, parent)
	);
	db.delete(m
	    .select(SupplyResourceContractTemperatureChart.class, "uuid")
	    .where(SupplyResourceContractTemperatureChart.c.UUID_XL, parent)
	);

	db.update(SupplyResourceContractSubject.class, HASH(
	    SupplyResourceContractSubject.c.ID_LOG, null,
	    SupplyResourceContractSubject.c.UUID_XL, parent
	), SupplyResourceContractSubject.c.UUID_XL.lc());
	db.delete(m
	    .select(SupplyResourceContractSubjectLog.class, "uuid")
	    .where(SupplyResourceContractSubject.c.UUID_XL, parent)
	);
	db.delete(m
	    .select(SupplyResourceContractSubject.class, "uuid")
	    .where(SupplyResourceContractSubject.c.UUID_XL, parent)
	);

	killXlSupplyResourceContractObjects(db, parent);

	db.update(SupplyResourceContract.class, HASH(
	    SupplyResourceContract.c.ID_LOG, null,
	    SupplyResourceContract.c.UUID_XL, parent
	), SupplyResourceContract.c.UUID_XL.lc());
	db.delete(m
	    .select(SupplyResourceContractLog.class, "uuid")
	    .where(SupplyResourceContract.c.UUID_XL, parent)
	);
        db.delete (m
            .select (SupplyResourceContract.class, "uuid")
            .where (SupplyResourceContract.c.UUID_XL, parent)
        );

	db.update(InXlSupplyResourceContract.class, HASH(
	    SupplyResourceContract.c.UUID_PERSON_CUSTOMER, null,
	    SupplyResourceContract.c.UUID_XL, parent
	), SupplyResourceContract.c.UUID_XL.lc());
	db.update(VocPerson.class, HASH(
	    VocPerson.c.ID_LOG, null,
	    VocPerson.c.UUID_XL, parent
	), VocPerson.c.UUID_XL.lc());
	db.delete(m
	    .select(VocPersonLog.class, "uuid")
	    .where(VocPerson.c.UUID_XL, parent)
	);
	db.delete(m
	    .select(VocPerson.class, "uuid")
	    .where(VocPerson.c.UUID_XL, parent)
	);
    }

    private void killXlSupplyResourceContractObjects(DB db, UUID parent) throws SQLException {

	final Model m = db.getModel();

	db.delete(m
	    .select(ResidentialPremise.class, "uuid")
	    .where("uuid_xl", parent)
	);
	db.delete(m
	    .select(NonResidentialPremise.class, "uuid")
	    .where("uuid_xl", parent)
	);
	db.delete(m
	    .select(Block.class, "uuid")
	    .where(Block.c.UUID_XL, parent)
	);

	db.update(SupplyResourceContractObject.class, HASH(
	    SupplyResourceContractObject.c.ID_LOG, null,
	    SupplyResourceContractObject.c.UUID_XL, parent
	), SupplyResourceContractObject.c.UUID_XL.lc());
	db.delete(m
	    .select(SupplyResourceContractObjectLog.class, "uuid")
	    .where(SupplyResourceContractObject.c.UUID_XL, parent)
	);
	db.delete(m
	    .select(SupplyResourceContractObject.class, "uuid")
	    .where(SupplyResourceContractObject.c.UUID_XL, parent)
	);
    }

    protected Map<String, Map<String, Object>> processVocLines(XSSFWorkbook wb, UUID uuid, DB db) throws XLException {

	Map<String, Map<String, Object>> vocs = new HashMap<String, Map<String, Object>> ();


	final XSSFSheet sheetVocNsi58  = wb.getSheet("Справочник оснований заключения");
	final XSSFSheet sheetVocNsi95  = wb.getSheet("Справочник видов документов");
	final XSSFSheet sheetVocNsi3   = wb.getSheet("Справочник КУ");
	final XSSFSheet sheetVocNsi236 = wb.getSheet("Справочник КР");
	final XSSFSheet sheetVocNsi276 = wb.getSheet("Справочник показателей качества");

	if (sheetVocNsi58 == null) {
	    throw new XLException("Отсутствует лист Справочник оснований заключения");
	}
	if (sheetVocNsi95 == null) {
	    throw new XLException("Отсутствует лист Справочник видов документов");
	}
	if (sheetVocNsi3 == null) {
	    throw new XLException("Отсутствует лист Справочник КУ");
	}
	if (sheetVocNsi236 == null) {
	    throw new XLException("Отсутствует лист Справочник КР");
	}
	if (sheetVocNsi276 == null) {
	    throw new XLException("Отсутствует лист Справочник показателей качества");
	}

	vocs.put("vc_nsi_95",  processIDLines(sheetVocNsi95));
	vocs.put("vc_nsi_3",   processVocNsi3Lines(sheetVocNsi3));
	vocs.put("vc_nsi_58",  processVocNsi58Lines(sheetVocNsi58));
	vocs.put("vc_nsi_236", processVocNsi236Lines(sheetVocNsi236));
	vocs.put("vc_nsi_239", vocs.get("vc_nsi_236"));
	vocs.put("vc_nsi_276", processVocNsi276Lines(sheetVocNsi276));

	logger.info("vocs=" + DB.to.json(vocs));

	return vocs;
    }

    protected void processLines (XSSFWorkbook wb, UUID uuid, DB db) throws Exception {

	Map<String, Map<String, Object>> vocs = processVocLines(wb, uuid, db);
        
        final XSSFSheet sheetContracts = wb.getSheetAt (0);
	final XSSFSheet sheetSubjects = wb.getSheetAt(1);
	final XSSFSheet sheetObjects = wb.getSheetAt(2);
	final XSSFSheet sheetServices = wb.getSheetAt(3);
	final XSSFSheet sheetQuality = wb.getSheetAt(4);
	final XSSFSheet sheetOtherQuality = wb.getSheetAt(5);
	final XSSFSheet sheetVolumes = wb.getSheetAt(6);
	final XSSFSheet sheetTemperatureCharts = wb.getSheetAt(7);

	boolean isOk = true;

        addContractLines(sheetContracts, uuid, db, vocs);
        if (!checkContractLines(sheetContracts, db, uuid)) {
	    isOk = false;
	}

	addSubjectLines(sheetSubjects, uuid, db, vocs);
	if (!checkSubjectLines(sheetSubjects, db, uuid)) {
	    isOk = false;
	}

	addObjectLines(sheetObjects, uuid, db);
	if (!checkObjectLines(sheetObjects, db, uuid)) {
	    isOk = false;
	}

	addServiceLines(sheetServices, uuid, db, vocs);
	if (!checkServiceLines(sheetServices, db, uuid)) {
	    isOk = false;
	}

	addQualityLines(sheetQuality, uuid, db, vocs);
	if (!checkQualityLines(sheetQuality, db, uuid)) {
	    isOk = false;
	}

	addOtherQualityLines(sheetOtherQuality, uuid, db, vocs);
	if (!checkOtherQualityLines(sheetOtherQuality, db, uuid)) {
	    isOk = false;
	}

	addVolumeLines(sheetVolumes, uuid, db, vocs);
	if (!checkVolumeLines(sheetVolumes, db, uuid)) {
	    isOk = false;
	}

	addTemperatureChartLines(sheetTemperatureCharts, uuid, db, vocs);
	if (!checkTemperatureChartLines(sheetTemperatureCharts, db, uuid)) {
	    isOk = false;
	}

        if (!isOk) throw new XLException ("ParseSupplyResourceContractsMDB NOT OK");
    }
}
