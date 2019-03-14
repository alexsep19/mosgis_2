package ru.eludia.products.mosgis.db.model.incoming.xl.lines;

import java.util.Map;
import java.util.UUID;
import org.apache.poi.xssf.usermodel.XSSFRow;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import ru.eludia.base.model.def.Def;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnTable;
import static ru.eludia.products.mosgis.db.model.incoming.xl.lines.InXlSupplyResourceContract.toCountingResource;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContractObject;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.db.model.voc.VocGisSupplyResourceContractCustomerType;
import ru.eludia.products.mosgis.db.model.voc.VocUnomStatus;
import ru.eludia.products.mosgis.jms.xl.base.XLException;

public class InXlSupplyResourceContractObject extends EnTable {

    public enum c implements ColEnum {

        ORD                     (Type.NUMERIC, 5, "Номер строки"),

	CODE_SR_CTR             (Type.STRING, null, "Иной код договора"),
	UUID_SR_CTR             (Type.UUID, null, "Договор"),

	IS_CONDO                (Type.BOOLEAN, null, "1 для МКД, 0 для ЖД"),
	HASBLOCKS               (Type.BOOLEAN, Bool.FALSE, "1 для жилых домов блокированной застройки, иначе 0"),
	ADDRESS                 (Type.STRING, null, "Адрес"),
        UNOM                    (Type.NUMERIC, 15, null, "UNOM"),


	PREMISE_CLASS           (Type.STRING, null, "Тип помещения: 1 - жилое, 2 - нежилое, 3 - блок"),
	APARTMENTNUMBER         (Type.STRING, null, "Номер помещения / Номер блока"),
	ROOMNUMBER              (Type.STRING, null, "Номер комнаты"),
	CODE_VC_NSI_30          (Type.STRING, 20, null, "Характеристика помещения"),
	IS_ADD                  (Type.BOOLEAN, null, "1 для добавить помещение/комнату, 0 не добавлять"),
	UUID_HOUSE              (Type.UUID, null, "Ссылка на паспорт дома"),

	TOTALAREA               (Type.NUMERIC, 25, 4, null, "Общая площадь помещения/блока"),
	ENTRANCENUM             (Type.STRING, null, "Номер подъезда"),
	UUID_ENTRANCE           (Type.UUID, null, "Ссылка на подъезд"),

	COUNTINGRESOURCE        (Type.BOOLEAN, null, "1, если РСО размещает информацию о начислениях за коммунальные услуги; 0 - размещает Исполнитель коммунальных услуг."),
	MDINFO                  (Type.BOOLEAN, null, "1, если РСО размещает информацию об индивидуальных приборах учета и их показаниях; иначе 0"),

	ERR                     (Type.STRING,  null, "Ошибка"),

        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
    }
    
    public static Map<String, Object> toHash (UUID uuid, int ord, XSSFRow row) {
        
        Map<String, Object> r = DB.HASH (
            EnTable.c.IS_DELETED, 1,
            SupplyResourceContractObject.c.UUID_XL, uuid,
            c.ORD, ord    
        );
        
        try {
            setFields (r, row);
        }         
        catch (XLException ex) {
            r.put (c.ERR.lc (), ex.getMessage ());
        }
        
        return r;
        
    }

    private static void setFields (Map<String, Object> r, XSSFRow row) throws XLException {

	r.put(c.CODE_SR_CTR.lc(), toString(row, 0, "Не указан Иной код договора (столбец A)"));


	Object is_condo = toString(row, 1);

	if (is_condo != null) {
	    String condo = is_condo.toString().toLowerCase();
	    if (condo.startsWith ("мкд")) {
		r.put(c.IS_CONDO.lc(), 1);
	    }
	    if (condo.startsWith("жд")) {
		r.put(c.IS_CONDO.lc(), 0);
	    }
	    if (condo.startsWith("жд блок. застройки")) {
		r.put(c.IS_CONDO.lc(), 0);
		r.put(c.HASBLOCKS.lc(), 1);
	    }
	}


	r.put(c.ADDRESS.lc(), toString(row, 2, "Не указан Адрес (столбец C)"));


	Object guid_or_unom = toString(row, 3, "Не указан UNOM (столбец D)");

	try {
	    UUID guid = UUID.fromString(guid_or_unom.toString());
	    r.put(SupplyResourceContractObject.c.FIASHOUSEGUID.lc(), guid);
	    r.put(c.UNOM.lc(), null);
	} catch (IllegalArgumentException ex) {
	    r.put(c.UNOM.lc(), guid_or_unom);
	}


	r.put(c.APARTMENTNUMBER.lc(), toString(row, 4));

	r.put(c.ROOMNUMBER.lc(), toString(row, 5));


	Object apartment_type = toString(row, 6);

	if (apartment_type != null) {
	    String kind = apartment_type.toString().toLowerCase();

	    boolean hasblocks = DB.ok(r.get(c.HASBLOCKS.lc()));

	    switch (kind) {
		case "нежилое помещение":
		    if (hasblocks) {
			throw new XLException("Некорректная Характеристика помещения (Столбец G): блок не бывает нежилым");
		    }
		    r.put(c.PREMISE_CLASS.lc(), 2);
		    break;
		case "отдельная квартира":
		    r.put(c.PREMISE_CLASS.lc(), 1);
		    r.put(c.CODE_VC_NSI_30.lc(), 1);
		    break;
		case "коммунальная квартира":
		    r.put(c.PREMISE_CLASS.lc(), 1);
		    r.put(c.CODE_VC_NSI_30.lc(), 2);
		    break;
		case "общежитие":
		    r.put(c.PREMISE_CLASS.lc(), 1);
		    r.put(c.CODE_VC_NSI_30.lc(), 3);
		    break;
		default:
		    throw new XLException("Некорректная Характеристика помещения (Столбец G): " + kind);
	    }

	    if (hasblocks) {
		r.put(c.PREMISE_CLASS.lc(), 3);
	    }
	}

	r.put(c.IS_ADD.lc(), toBool(row, 7));

	Object entrancenum = toString(row, 8);

	if (entrancenum != null && !entrancenum.toString().toLowerCase().equals("отдельный вход")) {
	    r.put(c.ENTRANCENUM.lc(), toString(row, 8));
	}


	r.put(c.TOTALAREA.lc(), toNumeric(row, 9));


	r.put(c.COUNTINGRESOURCE.lc(), toCountingResource(row, 10));

	r.put(c.MDINFO.lc(), toBool(row, 11));
    }

    public InXlSupplyResourceContractObject () {

        super ("in_xl_sr_ctr_obj", "Строки импорта объектов жилищного фонда ДРСО");

        cols  (c.class);

	for (ColEnum o : SupplyResourceContractObject.c.values()) {

	    SupplyResourceContractObject.c c = (SupplyResourceContractObject.c) o;

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

        key ("uuid_xl", SupplyResourceContractObject.c.UUID_XL);
        
        trigger ("BEFORE INSERT", ""
                
            + "DECLARE "
            + " in_ctr   in_xl_sr_ctr%ROWTYPE; "
	    + " house    tb_houses%ROWTYPE; "
	    + " entrance tb_entrances%ROWTYPE; "
	    + " room RAW(16); "
            + "BEGIN "
            + " IF :NEW.err IS NOT NULL THEN RETURN; END IF; "
                
	    + " BEGIN "
	    + "  SELECT * INTO in_ctr FROM in_xl_sr_ctr WHERE uuid_xl = :NEW.uuid_xl AND code = :NEW.code_sr_ctr; "
	    + "  EXCEPTION WHEN NO_DATA_FOUND THEN raise_application_error (-20000, 'Отсутствуют сведения по иному коду на вкладке \"Договоры ресурсоснабжения\"'); "
	    + " END; "
	    + " IF in_ctr.err IS NOT NULL THEN raise_application_error (-20000, 'Некорректное значение на вкладке \"Договоры ресурсоснабжения\"'); END IF; "

	    + " :NEW.uuid_sr_ctr := in_ctr.uuid; "

	    + " IF :NEW.fiashouseguid IS NOT NULL THEN BEGIN "
	    + "   SELECT houseguid INTO :NEW.fiashouseguid FROM vc_buildings WHERE houseguid = :NEW.fiashouseguid; "
	    + "   EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Не нашелся GUID ФИАС'); "
	    + " END; END IF; "

            + " IF :NEW.unom IS NOT NULL THEN BEGIN "
            + "   SELECT fiashouseguid INTO :NEW.fiashouseguid FROM vc_unom WHERE unom = :NEW.unom AND id_status = " + VocUnomStatus.i.OK+ "; "
            + "   EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Некорректный UNOM: не удалось однозначно определить GUID ФИАС');"
            + " END; END IF; "


	    + " IF :NEW.is_condo IS NOT NULL THEN BEGIN "
	    + "   BEGIN "
	    + "     SELECT * INTO house FROM tb_houses WHERE fiashouseguid = :NEW.fiashouseguid; "
	    + "     EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Не удалось однозначно определить паспорт дома'); "
	    + "   END; "
	    + "   :NEW.uuid_house := house.uuid; "
	    + "   IF house.is_condo <> :NEW.is_condo THEN "
	    + "     raise_application_error (-20000, 'Найден паспорт дома другого типа'); "
	    + "   END IF; "
	    + "   IF :NEW.is_condo = 0 AND house.hasblocks <> :NEW.hasblocks THEN "
	    + "     raise_application_error (-20000, 'ЖД с заданным адресом ' "
	    + "       || CASE WHEN house.hasblocks = 0 THEN 'не ' ELSE '' END "
	    + "       || 'является домом блокированной застройки'); "
	    + "   END IF; "
	    + " END; END IF; "

	    + " IF :NEW.apartmentnumber IS NOT NULL AND :NEW.uuid_premise IS NULL THEN BEGIN "

	    + "   IF :NEW.is_condo = 1 AND :NEW.code_vc_nsi_30 IS NULL THEN "
	    + "     raise_application_error (-20000, 'Заполните характеристику помещения МКД'); "
	    + "   END IF; "
	    + "   IF :NEW.is_add = 1 AND :NEW.code_vc_nsi_30 IS NULL THEN "
	    + "     raise_application_error (-20000, 'Заполните характеристику добавляемого помещения'); "
	    + "   END IF; "
	    + "   IF :NEW.is_condo = 0 AND :NEW.code_vc_nsi_30 != 1 THEN "
	    + "     raise_application_error (-20000, 'Для ЖД разрешена только характеристика помещения \"Отдельная квартира\"'); "
	    + "   END IF; "
	    + "   IF in_ctr.id_customer_type IN ("
	    + VocGisSupplyResourceContractCustomerType.i.ORGANIZATION
	    + "," + VocGisSupplyResourceContractCustomerType.i.OFFER
	    + ") THEN "
	    + "     raise_application_error (-20000, 'Договоры "
	    + VocGisSupplyResourceContractCustomerType.i.ORGANIZATION.getLabel()
	    + " и " + VocGisSupplyResourceContractCustomerType.i.OFFER.getLabel()
	    + " создаются на дом а не на квартиру. Очистите номер квартиры'); "
	    + "   END IF; "
	    + "   IF :NEW.is_condo = 0 AND :NEW.hasblocks = 0 AND :NEW.is_add = 1 THEN "
	    + "     raise_application_error (-20000, 'Дом является ЖД. Очистите поле Добавить помещение'); "
	    + "   END IF; "

	    + "   IF :NEW.premise_class = 1 THEN BEGIN "
	    + "     SELECT uuid INTO :NEW.uuid_premise FROM tb_premises_res WHERE is_deleted = 0 AND uuid_house = house.uuid AND premisesnum = :NEW.apartmentnumber; "
	    + "     EXCEPTION WHEN OTHERS THEN :NEW.uuid_premise := NULL; "
	    + "   END; END IF; "
	    + "   IF :NEW.premise_class = 2 THEN BEGIN "
	    + "     SELECT uuid INTO :NEW.uuid_premise FROM tb_premises_nrs WHERE is_deleted = 0 AND uuid_house = house.uuid AND premisesnum = :NEW.apartmentnumber; "
	    + "     EXCEPTION WHEN OTHERS THEN :NEW.uuid_premise := NULL; "
	    + "   END; END IF; "
	    + "   IF :NEW.premise_class = 3 THEN BEGIN "
	    + "     SELECT uuid INTO :NEW.uuid_premise FROM tb_blocks WHERE is_deleted = 0 AND uuid_house = house.uuid AND blocknum = :NEW.apartmentnumber; "
	    + "     EXCEPTION WHEN OTHERS THEN :NEW.uuid_premise := NULL; "
	    + "   END; END IF; "
	    + "   IF :NEW.entrancenum IS NOT NULL THEN BEGIN "
	    + "     SELECT uuid INTO :NEW.uuid_entrance FROM tb_entrances WHERE is_deleted = 0 AND uuid_house = house.uuid AND entrancenum = :NEW.entrancenum; "
	    + "     EXCEPTION WHEN OTHERS THEN raise_application_error (-20000, 'Не удалось однозначно определить подъезд'); "
	    + "   END; END IF; "

	    + "   IF :NEW.is_add = 0 AND :NEW.uuid_premise IS NULL THEN "
	    + "       raise_application_error (-20000, 'Не удалось однозначно определить помещение'); "
	    + "   END IF; "

	    + "   IF :NEW.roomnumber IS NOT NULL AND :NEW.uuid_premise IS NOT NULL THEN BEGIN "
	    + "     IF room IS NULL THEN BEGIN "
	    + "       SELECT r.uuid INTO room FROM tb_living_rooms r WHERE r.is_deleted = 0 AND r.uuid_house = house.uuid AND r.uuid_premise = :NEW.uuid_premise AND r.roomnumber = :NEW.roomnumber; "
	    + "       EXCEPTION WHEN NO_DATA_FOUND THEN room := NULL; "
	    + "     END; END IF; "
	    + "     IF room IS NULL THEN BEGIN "
	    + "       SELECT r.uuid INTO room FROM tb_living_rooms r WHERE r.is_deleted = 0 AND r.uuid_house = house.uuid AND r.uuid_block = :NEW.uuid_premise AND r.roomnumber = :NEW.roomnumber; "
	    + "       EXCEPTION WHEN NO_DATA_FOUND THEN room := NULL; "
	    + "     END; END IF; "
	    + "     IF room IS NOT NULL THEN "
	    + "       :NEW.uuid_premise := room; "
	    + "     ELSE "
	    + "        raise_application_error (-20000, 'Не удалось однозначно определить комнату'); "
	    + "     END IF; "
	    + "   END; END IF; " // IF :NEW.roomnumber IS NOT NULL
	    + " END; END IF; " // IF :NEW.apartmentnumber IS NOT NULL

            + " EXCEPTION WHEN OTHERS THEN "
            + " :NEW.err := REPLACE(SUBSTR(SQLERRM, 1, 1000), 'ORA-20000: ', ''); "

            + "END;"

        );
        
        StringBuilder sb = new StringBuilder ();
        StringBuilder nsb = new StringBuilder ();
        
        for (SupplyResourceContractObject.c c: SupplyResourceContractObject.c.values ()) if (c.isToXlImport()) {
            sb.append (',');
            sb.append (c.lc ());
            nsb.append (",:NEW.");
            nsb.append (c.lc ());
        }        

        trigger ("BEFORE UPDATE", ""

            + "DECLARE"
	    + " log         RAW (16); "
	    + " usr         RAW (16); "
	    + " org         RAW (16); "
	    + " premise     RAW (16); "
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
            + "BEGIN "

            + " IF :NEW.err IS NOT NULL THEN :NEW.is_deleted := 1; END IF; "
            + " IF NOT (:OLD.is_deleted = 1 AND :NEW.is_deleted = 0) THEN RETURN; END IF; "

	    + " IF :NEW.apartmentnumber IS NOT NULL AND :NEW.uuid_premise IS NULL AND :NEW.is_add = 1 THEN BEGIN "
	    + "   IF :NEW.premise_class = 1 THEN "
	    + "     INSERT INTO tb_premises_res(is_deleted,uuid_xl,uuid_house,premisesnum,totalarea,code_vc_nsi_30,uuid_entrance) "
	    + "     VALUES (0,:NEW.uuid_xl,:NEW.uuid_house,:NEW.apartmentnumber,:NEW.totalarea,:NEW.code_vc_nsi_30,:NEW.uuid_entrance) "
	    + "     RETURNING uuid INTO :NEW.uuid_premise; "
	    + "   END IF; "
	    + "   IF :NEW.premise_class = 2 THEN "
	    + "     INSERT INTO tb_premises_nrs(is_deleted,uuid_xl,uuid_house,premisesnum,totalarea) "
	    + "     VALUES (0,:NEW.uuid_xl,:NEW.uuid_house,:NEW.apartmentnumber,:NEW.totalarea) "
	    + "     RETURNING uuid INTO :NEW.uuid_premise; "
	    + "   END IF; "
	    + "   IF :NEW.premise_class = 3 THEN "
	    + "     INSERT INTO tb_blocks(is_deleted,uuid_xl,uuid_house,blocknum,totalarea,code_vc_nsi_30) "
	    + "     VALUES (0,:NEW.uuid_xl,:NEW.uuid_house,:NEW.apartmentnumber,:NEW.totalarea,:NEW.code_vc_nsi_30) "
	    + "     RETURNING uuid INTO :NEW.uuid_premise; "
	    + "   END IF; "
	    + " END; END IF; "

	    + " INSERT INTO tb_sr_ctr_obj (uuid,uuid_sr_ctr,is_deleted" + sb + ") VALUES (:NEW.uuid,:NEW.uuid_sr_ctr,0" + nsb + "); "

	    + " SELECT uuid_user, uuid_org INTO usr, org FROM in_xl_files WHERE uuid = :NEW.uuid_xl; "
	    + " INSERT INTO tb_sr_ctr_obj__log (action, uuid_object, uuid_user) "
	    + " VALUES ('" + VocAction.i.IMPORT_FROM_FILE.getName() + "', :NEW.uuid, usr) "
	    + " RETURNING uuid INTO log; "
	    + " UPDATE tb_sr_ctr_obj SET id_log = log, is_deleted = 1 WHERE uuid = :NEW.uuid; "

            + " COMMIT; "

            + "END; "

        );        

    }

}