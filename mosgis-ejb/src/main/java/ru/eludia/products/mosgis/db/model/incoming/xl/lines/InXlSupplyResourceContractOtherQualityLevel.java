package ru.eludia.products.mosgis.db.model.incoming.xl.lines;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
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
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractOtherQualityLevel;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractSubject;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisContractDimension;
import ru.eludia.products.mosgis.db.model.voc.VocGisContractQualityLevelType;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.jms.xl.base.XLException;

public class InXlSupplyResourceContractOtherQualityLevel extends EnTable {

    public enum c implements ColEnum {

	ORD                     (Type.NUMERIC, 5, "Номер строки"),

	CODE_SR_CTR             (Type.STRING, null, "Иной код договора"),
	UUID_SR_CTR             (Type.UUID, null, "Ссылка на Договор"),
	UUID_SR_CTR_SUBJ        (Type.UUID, null, "Поставляемый ресурс, предмет договора"),

	ADDRESS                 (Type.STRING, null, "Адрес"),
	APARTMENTNUMBER         (Type.STRING, null, "Номер помещения / Номер блока"),
	ROOMNUMBER              (Type.STRING, null, "Номер комнаты"),

	SERVICETYPE             (Type.STRING, 255, null, "Коммунальная услуга"),
	MUNICIPALRESOURCE       (Type.STRING, 255, null, "Коммунальный ресурс"),

	ERR                     (Type.STRING, null, "Ошибка"),
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
    }

    private static final Logger logger = Logger.getLogger(InXlSupplyResourceContractOtherQualityLevel.class.getName());

    public static Map<String, Object> toHash (UUID uuid, int ord, XSSFRow row, Map<String, Map<String, Object>> vocs) {
        
        Map<String, Object> r = DB.HASH (
            EnTable.c.IS_DELETED, 1,
            SupplyResourceContractOtherQualityLevel.c.UUID_XL, uuid,
            c.ORD, ord    
        );
        
        try {
            setFields (r, row, vocs);
        }
        catch (XLException ex) {
            r.put (c.ERR.lc (), ex.getMessage ());
        }

        return r;
        
    }

    private static void setFields (Map<String, Object> r, XSSFRow row, Map<String, Map<String, Object>> vocs) throws XLException {

	r.put(c.CODE_SR_CTR.lc(), toString(row, 0, "Не указан Иной код договора (столбец A)"));

	r.put(c.ADDRESS.lc(), toString(row, 1));

	r.put(c.APARTMENTNUMBER.lc(), toString(row, 2));

	r.put(c.ROOMNUMBER.lc(), toString(row, 3));


	Object servicetype = toString(row, 4, "Не указана Коммунальная услуга(столбец E)");

	r.put(c.SERVICETYPE.lc(), servicetype);
	
	r.put(SupplyResourceContractOtherQualityLevel.c.CODE_VC_NSI_3.lc(), vocs.get("vc_nsi_3").get(servicetype));


	Object municipalresource = toString(row, 5, "Не указан Коммунальный ресурс(столбец F)");

	r.put(c.MUNICIPALRESOURCE.lc(), municipalresource);

	r.put(SupplyResourceContractOtherQualityLevel.c.CODE_VC_NSI_239.lc(), vocs.get("vc_nsi_239").get(municipalresource));

	r.put(SupplyResourceContractOtherQualityLevel.c.LABEL.lc(), toString(row, 6));

	r.put(SupplyResourceContractOtherQualityLevel.c.ID_TYPE.lc()
	    , VocGisContractQualityLevelType.i.forLabel(DB.to.String(toString(row, 7)))
	);

	r.put(SupplyResourceContractOtherQualityLevel.c.INDICATORVALUE.lc(), toNumeric(row, 8));

	r.put(SupplyResourceContractOtherQualityLevel.c.INDICATORVALUE_FROM.lc(), toNumeric(row, 9));

	r.put(SupplyResourceContractOtherQualityLevel.c.INDICATORVALUE_TO.lc(), toNumeric(row, 10));

	r.put(SupplyResourceContractOtherQualityLevel.c.INDICATORVALUE_IS.lc()
	    , DB.to.String(toString(row, 11)).toLowerCase().equals("соответствует")? 1 : 0
	);


	r.put(SupplyResourceContractOtherQualityLevel.c.CODE_VC_OKEI.lc(), toNumeric(row, 12));

	r.put(SupplyResourceContractOtherQualityLevel.c.ADDITIONALINFORMATION.lc(), toString(row, 13));
    }

    public InXlSupplyResourceContractOtherQualityLevel() {

	super("in_xl_sr_ctr_other_qls", "Строки импорта иных показателей качества поставляемых ресурсов ДРСО");

	cols(c.class);

	for (ColEnum o : SupplyResourceContractOtherQualityLevel.c.values()) {

	    SupplyResourceContractOtherQualityLevel.c c = (SupplyResourceContractOtherQualityLevel.c) o;

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

	key("uuid_xl", SupplyResourceContractOtherQualityLevel.c.UUID_XL);

	trigger("BEFORE INSERT", ""
	    + "DECLARE "
	    + " in_ctr     in_xl_sr_ctr%ROWTYPE; "
	    + " in_ctr_svc  in_xl_sr_ctr_svc%ROWTYPE; "
	    + " in_ctr_subj in_xl_sr_ctr_subj%ROWTYPE; "
	    + "BEGIN "
	    + " IF :NEW.err IS NOT NULL THEN RETURN; END IF; "

	    + " BEGIN "
	    + "  SELECT * INTO in_ctr FROM in_xl_sr_ctr WHERE uuid_xl = :NEW.uuid_xl AND code = :NEW.code_sr_ctr; "
	    + "  EXCEPTION WHEN NO_DATA_FOUND THEN raise_application_error (-20000, 'Отсутствуют сведения по иному коду на вкладке \"Договоры ресурсоснабжения\"'); "
	    + " END; "
	    + " IF in_ctr.err IS NOT NULL THEN raise_application_error (-20000, 'Некорректное значение на вкладке \"Договоры ресурсоснабжения\"'); END IF; "
	    + " :NEW.uuid_sr_ctr := in_ctr.uuid; "

	    + " IF :NEW.code_vc_nsi_3 IS NULL THEN BEGIN "
	    + "  SELECT code INTO :NEW.code_vc_nsi_3 FROM vc_nsi_3 WHERE isactual=1 AND label = :NEW.servicetype; "
	    + "  EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Не найдена услуга с названием ' || :NEW.servicetype); "
	    + " END; END IF; "

	    + " IF :NEW.code_vc_nsi_239 IS NULL THEN BEGIN "
	    + "  SELECT code INTO :NEW.code_vc_nsi_239 FROM vc_nsi_239 WHERE isactual=1 AND label = :NEW.municipalresource; "
	    + "  EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Не найден коммунальный ресурс с названием ' || :NEW.municipalresource); "
	    + " END; END IF; "

	    + " IF in_ctr.specqtyinds = " + VocGisContractDimension.i.BY_HOUSE + " THEN "
	    + "   IF :NEW.address IS NULL THEN raise_application_error (-20000, 'Показатели качества ведутся в разрезе ОЖФ. Укажите  адрес'); END IF; "
	    + "   BEGIN "
	    + "    SELECT * INTO in_ctr_svc "
	    + "    FROM in_xl_sr_ctr_svc "
	    + "    WHERE uuid_xl     = :NEW.uuid_xl "
	    + "      AND code_sr_ctr = :NEW.code_sr_ctr "
	    + "      AND address     = :NEW.address "
	    + "      AND NVL(apartmentnumber, '00') = NVL(:NEW.apartmentnumber, '00') "
	    + "      AND NVL(roomnumber, '00')      = NVL(:NEW.roomnumber, '00') "
	    + "   ; "
	    + "    EXCEPTION WHEN NO_DATA_FOUND THEN raise_application_error (-20000, 'Отсутствуют сведения на вкладке \"КУ и КР по ОЖФ\"'); "
	    + "   END; "
	    + "   IF in_ctr_svc.err IS NOT NULL THEN raise_application_error (-20000, 'Некорректное значение на вкладке \"КУ и КР по ОЖФ\"'); END IF; "
	    + "   :NEW.uuid_sr_ctr_subj := in_ctr_svc.uuid; "
	    + " END IF; "

	    + " IF in_ctr.specqtyinds = " + VocGisContractDimension.i.BY_CONTRACT + " THEN "
	    + "   IF :NEW.address IS NOT NULL THEN raise_application_error (-20000, 'Показатели качества ведутся в разрезе договора. Очистите адрес'); END IF; "
	    + "   IF :NEW.apartmentnumber IS NOT NULL THEN raise_application_error (-20000, 'Показатели качества ведутся в разрезе договора. Очистите номер помещения'); END IF; "
	    + "   IF :NEW.roomnumber IS NOT NULL THEN raise_application_error (-20000, 'Показатели качества ведутся в разрезе договора. Очистите номер комнаты'); END IF; "
	    + "   BEGIN "
	    + "    SELECT * INTO in_ctr_subj "
	    + "    FROM in_xl_sr_ctr_subj "
	    + "    WHERE uuid_xl     = :NEW.uuid_xl "
	    + "      AND code_sr_ctr = :NEW.code_sr_ctr "
	    + "      AND code_vc_nsi_3   = :NEW.code_vc_nsi_3 "
	    + "      AND code_vc_nsi_239 = :NEW.code_vc_nsi_239 "
	    + "   ; "
	    + "    EXCEPTION WHEN NO_DATA_FOUND THEN raise_application_error (-20000, 'Отсутствуют сведения на вкладке \"Предметы договора\"'); "
	    + "   END; "
	    + "   IF in_ctr_subj.err IS NOT NULL THEN raise_application_error (-20000, 'Некорректное значение на вкладке \"Предметы договора\"'); END IF; "
	    + "   :NEW.uuid_sr_ctr_subj := in_ctr_subj.uuid; "
	    + " END IF; "

	    + " EXCEPTION WHEN OTHERS THEN "
	    + " :NEW.err := REPLACE(SUBSTR(SQLERRM, 1, 1000), 'ORA-20000: ', ''); "
	    + "END;"
	);

	StringBuilder sb_insert = new StringBuilder();
	StringBuilder sb_fields = new StringBuilder();

	for (SupplyResourceContractOtherQualityLevel.c c : SupplyResourceContractOtherQualityLevel.c.values()) {
	    if (c.isToXlImport()) {
		String col = c.lc();
		sb_fields.append(", ");
		sb_fields.append(col);
		sb_insert.append(", ");
		sb_insert.append(":NEW." + col);
	    }
	}

	trigger("BEFORE UPDATE", ""
	    + "DECLARE"
	    + " log         RAW (16); "
	    + " usr         RAW (16); "
	    + " PRAGMA AUTONOMOUS_TRANSACTION; "
	    + "BEGIN "
	    + " IF :NEW.err IS NOT NULL THEN :NEW.is_deleted := 1; END IF; "
	    + " IF NOT (:OLD.is_deleted = 1 AND :NEW.is_deleted = 0) THEN RETURN; END IF; "

	    + " INSERT INTO tb_sr_ctr_other_qls (uuid,is_deleted,uuid_sr_ctr,uuid_sr_ctr_subj" + sb_fields + ") VALUES (:NEW.uuid,0,:NEW.uuid_sr_ctr,:NEW.uuid_sr_ctr_subj" + sb_insert + "); "
	    + " UPDATE tb_sr_ctr_other_qls SET is_deleted = 1 WHERE uuid = :NEW.uuid; "

	    + " COMMIT; "
	    + "END; "
	);

    }

}