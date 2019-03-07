package ru.eludia.products.mosgis.db.model.incoming.xl.lines;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Def;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisContractDimension;
import ru.eludia.products.mosgis.db.model.voc.VocGisSupplyResourceContractCustomerType;
import ru.eludia.products.mosgis.db.model.voc.nsi58.VocNsi58;
import ru.eludia.products.mosgis.jms.xl.base.XLException;

public class InXlSupplyResourceContract extends EnTable {

    public enum c implements ColEnum {

        ORD                     (Type.NUMERIC, 5, "Номер строки"),
        CODE                    (Type.STRING, 255, "Иной код договора"),

	CUSTOMER_TYPE           (Type.INTEGER, null, "Тип лица/организации: 1 - ФЛ, 2 - ЮЛ, 3 - ИП, 4 - Не указывать"),
	// Заказчик ФЛ
	SURNAME                 (Type.STRING, 256, null, "Фамилия"),
	FIRSTNAME               (Type.STRING, 256, null, "Имя"),
	PATRONYMIC              (Type.STRING, 256, null, "Отчество"),
	IS_FEMALE               (Type.BOOLEAN, null, "Пол"),
	BIRTHDATE               (Type.DATE,          null,       "Дата рождения"),
	SNILS                   (Type.NUMERIC, 11,   null,       "СНИЛС"),
	LABEL_VC_NSI_95         (Type.STRING, null, "Вид документа, удостоверяющего личность"),
	CODE_VC_NSI_95          (Type.STRING,  20,   null,      "Код документа, удостоверяющего личность (НСИ 95)"),
	SERIES                  (Type.STRING,  45,   null,      "Серия документа, удостоверяющего дичность"),
	NUMBER_                 (Type.STRING,  45,   null,      "Номер документа, удостоверяющего личность"),
	ISSUEDATE               (Type.DATE,          null,      "Дата выдачи документа, удостоверяющего личность"),

	// Заказчик ЮЛ
	OGRN                    (Type.NUMERIC, 15, null, "ОГРН"),
	INN                     (Type.NUMERIC, 12, null, "ИНН"),
	KPP                     (Type.NUMERIC,  9, null, "КПП"),

	CONTRACTBASE            (Type.STRING, null, "Основение заключения договора (ссылается на код в строке скрытого листа)"),

        ID_SR_CTR               (Type.UUID, null, "Идентификатор договора ресурсоснабжения"),

        ERR                     (Type.STRING, null, "Ошибка")
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
    }

    private static final Logger logger = Logger.getLogger(InXlSupplyResourceContract.class.getName());

    public static Map<String, Object> toHash (UUID uuid, int ord, XSSFRow row, Map<String, Map<String, Object>> vocs) {
        
        Map<String, Object> r = DB.HASH (
            EnTable.c.IS_DELETED, 1,
            SupplyResourceContract.c.UUID_XL, uuid,
            c.ORD, ord    
        );
        
        try {
            setFields (r, row, vocs);
        }         
        catch (XLException ex) {
            r.put (c.ERR.lc (), ex.getMessage ());
        }

	logger.info("InXlSupplyResourceContract.r=" + DB.to.json(r));

	return r;
    }

    private static void setFields (Map<String, Object> r, XSSFRow row, Map<String, Map<String, Object>> vocs) throws XLException {
	logger.log(Level.INFO, "InXlSupplyResourceContract.setFields start");

	r.put(c.CODE.lc(), toString(row, 0, "Не указан Иной код договора (столбец A)"));

	r.put(SupplyResourceContract.c.IS_CONTRACT.lc(), toBool(row, 1, "Не указан Является публичным договор (столбец B)"));

        try {
            final XSSFCell cell = row.getCell (2);
            if (cell == null) throw new XLException ("Не указан Номер договора (столбец C)");
            final String s = cell.getStringCellValue ();
            if (!DB.ok (s)) throw new XLException ("Не указан Номер договора (столбец C)");
            r.put(SupplyResourceContract.c.CONTRACTNUMBER.lc (), s);
        }
        catch (XLException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage());
        }

        try {
            final XSSFCell cell = row.getCell (3);
            if (cell == null) throw new XLException ("Не указана Дата заключения договора (столбец D)");
            final Date d = cell.getDateCellValue ();
            if (d == null) throw new XLException ("Не указана Дата заключения договора (столбец D)");
            r.put(SupplyResourceContract.c.SIGNINGDATE.lc (), d);
        }
        catch (XLException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new XLException (ex.getMessage());
        }

	try {
	    final XSSFCell cell = row.getCell(4);
	    if (cell == null) {
		throw new XLException("Не указана Дата вступления в силу договора (столбец E)");
	    }
	    final Date d = cell.getDateCellValue();
	    if (d == null) {
		throw new XLException("Не указана Дата вступления в силу договора (столбец E)");
	    }
	    r.put(SupplyResourceContract.c.EFFECTIVEDATE.lc(), d);
	} catch (XLException ex) {
	    throw ex;
	} catch (Exception ex) {
	    throw new XLException(ex.getMessage());
	}

	r.put(SupplyResourceContract.c.AUTOROLLOVER.lc(), DB.ok(toBool(row, 5))? 1 : 0);

	try {
	    final XSSFCell cell = row.getCell(6);
	    if (cell == null) {
		throw new XLException("Не указана Дата окончания действия (столбец G)");
	    }
	    final Date d = cell.getDateCellValue();
	    if (d == null) {
		throw new XLException("Не указана Дата окончания действия (столбец G)");
	    }
	    r.put(SupplyResourceContract.c.COMPLETIONDATE.lc(), d);
	} catch (XLException ex) {
	} catch (Exception ex) {
	    throw new XLException(ex.getMessage());
	}

	try {
	    final XSSFCell cell = row.getCell(7);
	    if (cell == null) {
		throw new XLException("Не указан Тип заказчика (столбец H)");
	    }
	    String s = cell.getStringCellValue();
	    if (!DB.ok(s)) {
		throw new XLException("Не указан Тип заказчика (столбец H)");
	    }

	    VocGisSupplyResourceContractCustomerType.i id_customer_type;

	    switch (s) {
		case "Собственник или пользователь жилого (нежилого) помещения в МКД":
		    id_customer_type = VocGisSupplyResourceContractCustomerType.i.OWNER;
		    break;
		case "Представитель собственников МКД":
		    id_customer_type = VocGisSupplyResourceContractCustomerType.i.REPRESENTATIVEOWNER;
		    break;
		case "Единоличный собственник помещений в МКД":
		    id_customer_type = VocGisSupplyResourceContractCustomerType.i.SOLEOWNER;
		    break;
		case "Собственник или пользователь жилого дома (домовладения)":
		    id_customer_type = VocGisSupplyResourceContractCustomerType.i.LIVINGHOUSEOWNER;
		    break;
		case "Управляющая организация":
		    id_customer_type = VocGisSupplyResourceContractCustomerType.i.ORGANIZATION;
		    break;
		default:
		    id_customer_type = VocGisSupplyResourceContractCustomerType.i.forLabel(s);
	    }

	    if (!DB.ok(id_customer_type)) {
		throw new XLException("Не найден Тип заказчика (столбец H): " + s);
	    }
	    r.put(SupplyResourceContract.c.ID_CUSTOMER_TYPE.lc(), id_customer_type.getId());
	} catch (XLException ex) {
	    throw ex;
	} catch (Exception ex) {
	    throw new XLException(ex.getMessage());
	}

	setFieldsCustomer(r, row, vocs);

	r.put(SupplyResourceContract.c.ONETIMEPAYMENT.lc(), toBool(row, 22));

	setFieldsDeadlines(r, row);

	r.put(SupplyResourceContract.c.VOLUMEDEPENDS.lc(), toBool(row, 29));

	setFieldsMdPeriod(r, row);

	try {
	    final XSSFCell cell = row.getCell(34);
	    if (cell == null) {
		throw new XLException("Не указано Основание заключения договора (столбец AI)");
	    }
	    final String s = cell.getStringCellValue();

	    if (!DB.ok(s)) {
		throw new XLException("Не указано Основание заключения договора (столбец AI)");
	    }

	    r.put(c.CONTRACTBASE.lc(), s);

	    r.put(SupplyResourceContract.c.CODE_VC_NSI_58.lc(), vocs.get("vc_nsi_58").get(s));

	} catch (XLException ex) {
	    throw ex;
	} catch (Exception ex) {
	    throw new XLException(ex.getMessage());
	}

	try {
	    final XSSFCell cell = row.getCell(35);
	    if (cell == null) {
		throw new XLException("Порядок размещения информации о начислениях за коммунальные услуги (столбец AJ)");
	    }
	    String s = cell.getStringCellValue();
	    if (!DB.ok(s)) {
		throw new XLException("Не указан Порядок размещения информации о начислениях за коммунальные услуги (столбец AJ)");
	    }

	    s = s.toLowerCase().replace(" ожф", " объектов жилищного фонда");

	    VocGisContractDimension.i accrualprocedure = VocGisContractDimension.i.forLabel(s);

	    if (!DB.ok(accrualprocedure)) {
		throw new Exception("Не найдено Порядок размещения информации о начислениях за коммунальные услуги (столбец AJ): " + s);
	    }

	    r.put(SupplyResourceContract.c.ACCRUALPROCEDURE.lc(), accrualprocedure.getId());
	} catch (XLException ex) {
	} catch (Exception ex) {
	    throw new XLException(ex.getMessage());
	}

	try {
	    final XSSFCell cell = row.getCell(36);
	    if (cell == null) {
		throw new XLException("Не указан Размещение информации о начислениях за коммунальные услуги осуществляет (столбец AK)");
	    }
	    final String s = cell.getStringCellValue();
	    if (!DB.ok(s)) {
		throw new XLException("Не указан Размещение информации о начислениях за коммунальные услуги осуществляет (столбец AK)");
	    }

	    r.put(SupplyResourceContract.c.COUNTINGRESOURCE.lc(), s.toLowerCase().contains("рсо")? 1 : 0);
	} catch (XLException ex) {
	} catch (Exception ex) {
	    throw new XLException(ex.getMessage());
	}

	r.put(SupplyResourceContract.c.MDINFO.lc(), toBool(row, 37));

	try {
	    final XSSFCell cell = row.getCell(38);
	    if (cell == null) {
		throw new XLException("Не указано Показатели качества коммунальных ресурсов ведутся (столбец AM)");
	    }
	    final String s = cell.getStringCellValue();
	    if (!DB.ok(s)) {
		throw new XLException("Не указан Показатели качества коммунальных ресурсов ведутся (столбец AM)");
	    }
	    VocGisContractDimension.i specqtyinds = VocGisContractDimension.i.forLabel(s);
	    if (!DB.ok(specqtyinds)) {
		throw new XLException("Не найдено Показатели качества коммунальных ресурсов ведутся (столбец AM): " + s);
	    }
	    r.put(SupplyResourceContract.c.SPECQTYINDS.lc(), specqtyinds.getId());
	} catch (XLException ex) {
	    throw ex;
	} catch (Exception ex) {
	    throw new XLException(ex.getMessage());
	}

	setFieldsPlannedVolume (r, row);

	try {
	    final XSSFCell cell = row.getCell(41);
	    if (cell != null) {
		final String s = cell.getStringCellValue();
		if (DB.ok(s)) {
		    r.put(c.ID_SR_CTR.lc(), UUID.fromString(s));
		}
	    }
	} catch (Exception ex) {
	    throw new XLException("Некорректный тип ячейки Идентификатор договора ресурсоснабжения (столбец AR)");
	}

	logger.log(Level.INFO, "InXlSupplyResourceContract.setFields end");
    }


    private static Object toIsFemale(XSSFRow row, int col) throws XLException {

	Object f = DB.to.String(toString(row, col));

	if (f == null) {
	    return null;
	}

	String s = DB.to.String(f);

	return s.equals("ж") ? 1 : s.equals("м") ? 0 : null;
    }

    private static void setFieldsPlannedVolume(Map<String, Object> r, XSSFRow row) throws XLException {

	r.put(SupplyResourceContract.c.ISPLANNEDVOLUME.lc(), toBool(row, 39,"Не указано Наличие в договоре планового объема (столбец AN)"));

	if (!DB.ok(r.get(SupplyResourceContract.c.ISPLANNEDVOLUME.lc()))) {
	    return;
	}

	try {
	    final XSSFCell cell = row.getCell(40);

	    if (cell == null) {
		throw new XLException("Указано Наличие в договоре планового объема=ДА но не указан Тип ведения планового объема (столбец AO)");
	    }
	    final String s = cell.getStringCellValue();
	    if (!DB.ok(s)) {
		throw new XLException("Не указан Тип ведения планового объема (столбец AO)");
	    }
	    VocGisContractDimension.i plannedvolumetype = VocGisContractDimension.i.forLabel(s);
	    if (!DB.ok(plannedvolumetype)) {
		throw new XLException("Не найдено Тип ведения планового объема (столбец AO): " + s);
	    }
	    r.put(SupplyResourceContract.c.PLANNEDVOLUMETYPE.lc(), plannedvolumetype.getId());
	} catch (XLException ex) {
	    throw ex;
	} catch (Exception ex) {
	    throw new XLException(ex.getMessage());
	}
    }

    private static int toCustomerType(XSSFRow row, int col) throws XLException {

	String s = DB.to.String(toString(row, col));

	String lower_s = s.toLowerCase();

	return lower_s.startsWith("физическое ") ? 1
	    : lower_s.startsWith("юридическое ") ? 2
	    : lower_s.startsWith("индивидуальный ") ? 3
	    : lower_s.startsWith("не указывать ") ? 4
	    : 0
	;
    }

    private static void setFieldsCustomer(Map<String, Object> r, XSSFRow row, Map<String, Map<String, Object>> vocs) throws XLException {

	VocGisSupplyResourceContractCustomerType.i id_customer_type
	    = VocGisSupplyResourceContractCustomerType.i.forId(r.get(SupplyResourceContract.c.ID_CUSTOMER_TYPE.lc()))
	;

	int customer_type = toCustomerType(row, 8);

	r.put(c.CUSTOMER_TYPE.lc(), customer_type);

	if (customer_type == 0 && !VocGisSupplyResourceContractCustomerType.i.OFFER.equals(id_customer_type)) {
	    throw new XLException("Не указан Тип лица/организации (столбец I)");
	}

	if (customer_type == 1) {
	    if (!id_customer_type.getIsPhy()) {
		throw new XLException("ФЛ не может быть заказчиком в договоре " + id_customer_type.getLabel());
	    }
	    setFieldsCustomerPerson(r, row, vocs);
	}

	if (customer_type == 2 || customer_type == 3) {
	    if (!id_customer_type.getIsJur()) {
		throw new XLException("ЮЛ не может быть заказчиком в договоре " +  id_customer_type.getLabel());
	    }
	    setFieldsCustomerOrg(r, row, vocs);
	}
    }

    private static void setFieldsCustomerPerson(Map<String, Object> r, XSSFRow row, Map<String, Map<String, Object>> vocs) throws XLException {

	r.put(c.SURNAME.lc(), toString(row, 9, "Не указано Фамилия (столбец J)"));

	r.put(c.FIRSTNAME.lc(), toString(row, 10, "Не указано Имя (столбец K)"));

	r.put(c.PATRONYMIC.lc(), toString(row, 11));


	r.put(c.IS_FEMALE.lc(), toIsFemale(row, 12));

	r.put(c.BIRTHDATE.lc(), toDate(row, 13));

	r.put(c.SNILS.lc(), toNumeric(row, 14));


	if (!DB.ok(r.get(c.SNILS.lc()))) {

	    final Object label_vc_nsi_95 = toString (row, 15,"Не указан Вид документа (столбец P)");

	    r.put(c.LABEL_VC_NSI_95.lc(), label_vc_nsi_95);

	    r.put(SupplyResourceContract.c.CODE_VC_NSI_58.lc(), vocs.get("vc_nsi_95").get(label_vc_nsi_95));

	    r.put(c.SERIES.lc(), toString(row, 17));

	    r.put(c.NUMBER_.lc(), toString(row, 16, "Не указан Номер документа (столбец Q)"));

	    r.put(c.ISSUEDATE.lc(), toDate(row, 18, "Не указана Дата выдачи документа (столбец S)"));
	}
    }

    private static void setFieldsCustomerOrg(Map<String, Object> r, XSSFRow row, Map<String, Map<String, Object>> vocs) throws XLException {

	r.put(c.OGRN.lc(), toNumeric(row, 19, "Не указан ОГРН (столбец T)"));

	r.put(c.INN.lc(), toNumeric(row, 20));

	r.put(c.KPP.lc(), toNumeric(row, 21, "Не указан КПП (столбец V)"));
    }

    public static Object toDeadlineNumNxt(XSSFRow row, int col) throws XLException {

	String s;

	try {
	    final XSSFCell cell = row.getCell(col);
	    if (cell == null) {
		return null;
	    }

	    s = cell.getCellTypeEnum() == CellType.NUMERIC ? DB.to.String(cell.getNumericCellValue())
		: cell.getStringCellValue().trim();

	    if (!DB.ok(s)) {
		return null;
	    }
	} catch (Exception ex) {
	    throw new XLException("col: " + col + " " + ex.getMessage());
	}

	return s.contains("текущ") ? 0 : s.contains("следующ") ? 1 : null;
    }

    public static Object toDeadlineNum(XSSFRow row, int col) throws XLException {

	Object result = toNumeric(row, col);

	if (DB.to.Long(result) == -1) {
	    return 99;
	}

	return result;
    }

    private static void setFieldsDeadlines(Map<String, Object> r, XSSFRow row) throws XLException {

	r.put(SupplyResourceContract.c.DDT_D_START.lc(), toDeadlineNum(row, 23));
	r.put(SupplyResourceContract.c.DDT_D_START_NXT.lc(), toDeadlineNumNxt(row, 24));

	r.put(SupplyResourceContract.c.DDT_I_START.lc(), toDeadlineNum(row, 25));
	r.put(SupplyResourceContract.c.DDT_I_START_NXT.lc(), toDeadlineNumNxt(row, 26));

	r.put(SupplyResourceContract.c.DDT_N_START.lc(), toDeadlineNum(row, 27));
	r.put(SupplyResourceContract.c.DDT_N_START_NXT.lc(), toDeadlineNumNxt(row, 28));
    }

    private static void setFieldsMdPeriod(Map<String, Object> r, XSSFRow row) throws XLException {

	r.put(SupplyResourceContract.c.DDT_M_START.lc(), toDeadlineNum(row, 30));
	r.put(SupplyResourceContract.c.DDT_M_START_NXT.lc(), toBool(row, 31));

	r.put(SupplyResourceContract.c.DDT_M_END.lc(), toDeadlineNum(row, 32));
	r.put(SupplyResourceContract.c.DDT_M_START_NXT.lc(), toBool(row, 33));
    }

    public InXlSupplyResourceContract () {

        super ("in_xl_sr_ctr", "Строки импорта ДРСО");

        cols  (c.class);

	for (ColEnum o : SupplyResourceContract.c.values()) {

	    SupplyResourceContract.c c = (SupplyResourceContract.c) o;

	    if (!c.isToXlImport()) {
		continue;
	    }

	    Col col = c.getCol().clone();

	    Def def = col.getDef();
	    boolean isVirtual = def != null && def instanceof Virt;

	    if (!isVirtual) {
		col.setDef(null);
		col.setNullable(true);
	    }

	    add(col);
	}

        key ("uuid_xl", SupplyResourceContract.c.UUID_XL);

        trigger ("BEFORE INSERT", ""
            + "DECLARE "
            + "BEGIN "

            + " SELECT uuid_org INTO :NEW.uuid_org FROM in_xl_files WHERE uuid=:NEW.uuid_xl; "

            + " IF :NEW.err IS NOT NULL THEN RETURN; END IF; "

	    + " IF :NEW.customer_type = 1 AND :NEW.uuid_person_customer IS NULL THEN BEGIN "
	    + "   IF :NEW.uuid_person_customer IS NULL THEN BEGIN "
	    + "     SELECT uuid INTO :NEW.uuid_person_customer FROM vc_persons WHERE is_deleted = 0 "
	    + "       AND surname        = :NEW.surname "
	    + "       AND firstname      = :NEW.firstname "
	    + "       AND snils          = :NEW.snils "
	    + "       ; "
	    + "       EXCEPTION WHEN NO_DATA_FOUND THEN :NEW.uuid_person_customer := NULL; "
	    + "   END; END IF;"
	    + "   IF :NEW.uuid_person_customer IS NULL THEN BEGIN "
	    + "     SELECT uuid INTO :NEW.uuid_person_customer FROM vc_persons WHERE is_deleted = 0 "
	    + "       AND surname        = :NEW.surname "
	    + "       AND firstname      = :NEW.firstname "
	    + "       AND code_vc_nsi_95 = :NEW.code_vc_nsi_95 "
	    + "       AND number_        = :NEW.number_ "
	    + "       AND NVL(series, '00') = NVL(:NEW.series, '00') "
	    + "       AND issuedate      = :NEW.issuedate "
	    + "       ; "
	    + "       EXCEPTION WHEN NO_DATA_FOUND THEN :NEW.uuid_person_customer := NULL; "
	    + "   END; END IF;"
	    + "   IF :NEW.uuid_person_customer IS NULL THEN "
	    + "     raise_application_error (-20000, 'Не удалось определить заказчика ФЛ'); "
	    + "   END IF;"
	    + " END; END IF; "

	    + " IF :NEW.customer_type = 2 AND :NEW.uuid_org_customer IS NULL THEN BEGIN "
	    + "   SELECT uuid INTO :NEW.uuid_org_customer FROM vc_orgs WHERE is_deleted = 0 AND id_type = 1 AND ogrn = :NEW.ogrn AND kpp = :NEW.kpp; "
	    + "   EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Не удалось определить заказчика ЮЛ по ОГРН ' || :NEW.ogrn || ' и КПП ' || :NEW.kpp); "
	    + " END; END IF; "

	    + " IF :NEW.customer_type = 3 THEN BEGIN "
	    + "   SELECT uuid INTO :NEW.uuid_org_customer FROM vc_orgs WHERE is_deleted = 0 AND id_type = -1 AND ogrn = :NEW.ogrn AND kpp = :NEW.kpp; "
	    + "   EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Не удалось определить заказчика ЮЛ ИП по ОГРН ' || :NEW.ogrn || ' и КПП ' || :NEW.kpp); "
	    + " END; END IF; "

	    + " EXCEPTION WHEN OTHERS THEN "
	    + " :NEW.err := REPLACE(SUBSTR(SQLERRM, 1, 1000), 'ORA-20000: ', ''); "

	    + "END;"

        );

	StringBuilder sb_insert = new StringBuilder();
	StringBuilder sb_fields = new StringBuilder();

        for (SupplyResourceContract.c c: SupplyResourceContract.c.values ()) if (c.isToXlImport ()) {
	    String col = c.lc();

	    sb_fields.append(", ");
	    sb_fields.append(col);

	    sb_insert.append(", ");
	    sb_insert.append(":NEW." + col);
        }

        trigger ("BEFORE UPDATE", ""

            + "DECLARE"
	    + " ctr         RAW (16); "
	    + " log         RAW (16); "
	    + " usr         RAW (16); "
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
            + "BEGIN "

            + " IF :NEW.err IS NOT NULL THEN :NEW.is_deleted := 1; END IF; "
            + " IF NOT (:OLD.is_deleted = 1 AND :NEW.is_deleted = 0) THEN RETURN; END IF; "

	    + " ctr := :NEW.uuid; "
	    + " INSERT INTO tb_sr_ctr (uuid,is_deleted" + sb_fields + ") VALUES (ctr,0" + sb_insert + "); "

	    + " SELECT uuid_user INTO usr FROM in_xl_files WHERE uuid = :NEW.uuid_xl; "
	    + " INSERT INTO tb_sr_ctr__log (action, uuid_object, uuid_user) "
	    + " VALUES ('" + VocAction.i.IMPORT_FROM_FILE.getName() + "', ctr, usr) "
	    + " RETURNING uuid INTO log; "
	    + " UPDATE tb_sr_ctr SET id_log = log, is_deleted = 1 WHERE uuid = :NEW.uuid; "
            + " COMMIT; "

	    + "END; "

        );

    }

}