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
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractSubject;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.jms.xl.base.XLException;

public class InXlSupplyResourceContractVolume extends EnTable {

    public enum c implements ColEnum {

	ORD                     (Type.NUMERIC, 5, "Номер строки"),

	CODE_SR_CTR             (Type.STRING, null, "Иной код договора"),
        UUID_SR_CTR             (Type.UUID, null, "Ссылка на Договор"),
	UUID_SR_CTR_SUBJ        (Type.UUID, null, "Ссылка на поставляемый ресурс"),

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

    private static final Logger logger = Logger.getLogger(InXlSupplyResourceContractVolume.class.getName());

    public static Map<String, Object> toHash (UUID uuid, int ord, XSSFRow row, Map<String, Map<String, Object>> vocs) {
        
        Map<String, Object> r = DB.HASH (
            EnTable.c.IS_DELETED, 1,
            SupplyResourceContractSubject.c.UUID_XL, uuid,
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

	r.put(c.ADDRESS.lc(), toString(row, 1, "Не указан Адрес ОЖФ (столбец B)"));

	r.put(c.APARTMENTNUMBER.lc(), toString(row, 2));

	r.put(c.ROOMNUMBER.lc(), toString(row, 3));


	Object servicetype = toString(row, 4, "Не указана Коммунальная услуга(столбец E)");

	r.put(c.SERVICETYPE.lc(), servicetype);
	
	r.put(SupplyResourceContractSubject.c.CODE_VC_NSI_3.lc(), vocs.get("vc_nsi_3").get(servicetype));


	Object municipalresource = toString(row, 5, "Не указан Коммунальный ресурс(столбец F)");

	r.put(c.MUNICIPALRESOURCE.lc(), municipalresource);

	r.put(SupplyResourceContractSubject.c.CODE_VC_NSI_239.lc(), vocs.get("vc_nsi_239").get(municipalresource));


	r.put(SupplyResourceContractSubject.c.VOLUME.lc(), toNumeric(row, 6));

	r.put(SupplyResourceContractSubject.c.UNIT.lc(), toNumeric(row, 7));

	r.put(SupplyResourceContractSubject.c.FEEDINGMODE.lc(), toString(row, 8));
    }

    public InXlSupplyResourceContractVolume() {

	super("in_xl_sr_ctr_vol", "Строки импорта планового объема поставляемых ресурсов ДРСО");

	cols(c.class);

	for (ColEnum o : SupplyResourceContractSubject.c.values()) {

	    SupplyResourceContractSubject.c c = (SupplyResourceContractSubject.c) o;

	    if (!c.isToXlImportVolume()) {
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

	key("uuid_xl", SupplyResourceContractSubject.c.UUID_XL);

	trigger("BEFORE INSERT", ""
	    + "DECLARE "
	    + " in_ctr     in_xl_sr_ctr%ROWTYPE; "
	    + " in_ctr_svc in_xl_sr_ctr_svc%ROWTYPE; "
	    + "BEGIN "
	    + " IF :NEW.err IS NOT NULL THEN RETURN; END IF; "

	    + " BEGIN "
	    + "  SELECT * INTO in_ctr FROM in_xl_sr_ctr WHERE uuid_xl = :NEW.uuid_xl AND code = :NEW.code_sr_ctr; "
	    + "  EXCEPTION WHEN NO_DATA_FOUND THEN raise_application_error (-20000, 'Отсутствуют сведения по иному коду на вкладке \"Договоры ресурсоснабжения\"'); "
	    + " END; "
	    + " IF in_ctr.err IS NOT NULL THEN raise_application_error (-20000, 'Некорректное значение на вкладке \"Договоры ресурсоснабжения\"'); END IF; "
	    + " :NEW.uuid_sr_ctr := in_ctr.uuid; "

	    + " BEGIN "
	    + "  SELECT * INTO in_ctr_svc "
	    + "  FROM in_xl_sr_ctr_svc "
	    + "  WHERE uuid_xl     = :NEW.uuid_xl "
	    + "    AND code_sr_ctr = :NEW.code_sr_ctr "
	    + "    AND address     = :NEW.address "
	    + "    AND NVL(apartmentnumber, '00') = NVL(:NEW.apartmentnumber, '00') "
	    + "    AND NVL(roomnumber, '00')      = NVL(:NEW.roomnumber, '00') "
	    + " ; "
	    + "  EXCEPTION WHEN NO_DATA_FOUND THEN raise_application_error (-20000, 'Отсутствуют сведения на вкладке \"КУ и КР по ОЖФ\"'); "
	    + " END; "
	    + " IF in_ctr_svc.err IS NOT NULL THEN raise_application_error (-20000, 'Некорректное значение на вкладке \"КУ и КР по ОЖФ\"'); END IF; "
	    + " :NEW.uuid_sr_ctr_subj := in_ctr_svc.uuid; "

	    + " IF :NEW.code_vc_nsi_3 IS NULL THEN BEGIN "
	    + "  SELECT code INTO :NEW.code_vc_nsi_3 FROM vc_nsi_3 WHERE isactual=1 AND label = :NEW.servicetype; "
	    + "  EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Не найдена услуга с названием ' || :NEW.servicetype); "
	    + " END; END IF; "

	    + " IF :NEW.code_vc_nsi_239 IS NULL THEN BEGIN "
	    + "  SELECT code INTO :NEW.code_vc_nsi_239 FROM vc_nsi_239 WHERE isactual=1 AND label = :NEW.municipalresource; "
	    + "  EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Не найден коммунальный ресурс с названием ' || :NEW.municipalresource); "
	    + " END; END IF; "

	    + " EXCEPTION WHEN OTHERS THEN "
	    + " :NEW.err := REPLACE(SUBSTR(SQLERRM, 1, 1000), 'ORA-20000: ', ''); "
	    + "END;"
	);

	StringBuilder sb_set = new StringBuilder();

	for (SupplyResourceContractSubject.c c : SupplyResourceContractSubject.c.values()) {
	    if (c.isToXlImportVolume()) {
		String col = c.lc();
		sb_set.append(", ");
		sb_set.append(col + " = :NEW." + col);
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

	    + " UPDATE tb_sr_ctr_subj SET is_deleted=0" + sb_set + " WHERE uuid = :NEW.uuid_sr_ctr_subj; "

	    + " SELECT uuid_user INTO usr FROM in_xl_files WHERE uuid = :NEW.uuid_xl; "
	    + " INSERT INTO tb_sr_ctr_subj__log (action, uuid_object, uuid_user) "
	    + " VALUES ('" + VocAction.i.IMPORT_FROM_FILE.getName() + "', :NEW.uuid_sr_ctr_subj, usr) "
	    + " RETURNING uuid INTO log; "
	    + " UPDATE tb_sr_ctr_subj SET id_log = log, is_deleted = 1 WHERE uuid = :NEW.uuid_sr_ctr_subj; "

	    + " COMMIT; "
	    + "END; "
	);

    }

}