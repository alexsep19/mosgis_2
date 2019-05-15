package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.STRING;
import static ru.eludia.base.model.Type.NUMERIC;

import static ru.eludia.base.model.Type.BOOLEAN;
import static ru.eludia.base.model.def.Bool.FALSE;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.voc.VocGisContractQualityLevelType;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.gosuslugi.dom.schema.integration.house_management.ExportSupplyResourceContractType;
import ru.gosuslugi.dom.schema.integration.house_management.ExportSupplyResourceContractObjectAddressResultType;

public class SupplyResourceContractQualityLevel extends EnTable {

    public static final String TABLE_NAME = "tb_sr_ctr_qls";

    public enum c implements EnColEnum {

	UUID_XL               (InXlFile.class, "Файл импорта"),

        UUID_SR_CTR           (SupplyResourceContract.class, null, "Договор ресурсоснабжения (заполняется всегда)"),

        UUID_SR_CTR_SUBJ      (SupplyResourceContractSubject.class, null, "Предмет договора, поставляемый ресурс ОЖФ"),

        CODE_VC_NSI_276       (STRING, 20, "Ссылка на НСИ \"Показатели качества коммунальных ресурсов\" (реестровый номер 276)"),
	ID_TYPE               (VocGisContractQualityLevelType.class, "Тип иного показателя качества"),

        QUALITYINDICATORREF   (STRING, new Virt("(''||\"CODE_VC_NSI_276\")"), "Наименование"),

        INDICATORVALUE        (NUMERIC, 20, 6, null, "Значение"),
        INDICATORVALUE_FROM   (NUMERIC, 20, 6, null, "Значение от"),
        INDICATORVALUE_TO     (NUMERIC, 20, 6, null, "Значение до"),
        INDICATORVALUE_IS     (BOOLEAN, FALSE, "Значение: 1 - если соответствует, иначе 0"),

        CODE_VC_OKEI          (VocOkei.class, null, "Единица измерения"),

        ADDITIONALINFORMATION (STRING, 500, null, "Дополнительная информация"),
        ;

        @Override
        public Col getCol() {
            return col;
        }
        private Col col;

        private c(Type type, Object... p) {
            col = new Col(this, type, p);
        }

        private c(Class c, Object... p) {
            col = new Ref(this, c, p);
        }

        @Override
        public boolean isLoggable() {
            return false;
        }

	public boolean isToXlImport() {

	    switch (this) {
	    case UUID_XL:
	    case CODE_VC_NSI_276:
	    case INDICATORVALUE:
	    case INDICATORVALUE_FROM:
	    case INDICATORVALUE_TO:
	    case INDICATORVALUE_IS:
	    case CODE_VC_OKEI:
	    case ADDITIONALINFORMATION:
		return true;
	    default:
		return false;
	    }
	}
    }

    public SupplyResourceContractQualityLevel () {

        super (TABLE_NAME, "Показатели качества договора РСО");

        cols  (c.class);

        key   ("uuid_sr_ctr_subj", c.UUID_SR_CTR_SUBJ);

        trigger("BEFORE INSERT OR UPDATE", ""
                + "DECLARE"
                + " PRAGMA AUTONOMOUS_TRANSACTION; "
                + "BEGIN "

                + "IF :NEW.is_deleted = 0 THEN BEGIN "

		    + "  IF :NEW.uuid_sr_ctr_subj IS NULL THEN "
		    + "     raise_application_error (-20000, 'Показатель качества должен быть привязан к предмету договора или к поставляемому ресурсу ОЖФ'); "
		    + "  END IF;"
		    + "  SELECT uuid_sr_ctr INTO :NEW.uuid_sr_ctr FROM tb_sr_ctr_subj WHERE uuid=:NEW.uuid_sr_ctr_subj; "

		    + "  BEGIN "
		    + "    SELECT code INTO :NEW.code_vc_nsi_276 FROM vc_nsi_276 WHERE isactual = 1 AND code = :NEW.code_vc_nsi_276;"
		    + "    EXCEPTION WHEN NO_DATA_FOUND THEN raise_application_error (-20000, 'Укажите показатель качества из справочника ГИС ЖКХ номер 276'); "
		    + "  END; "

		    + " SELECT id_type INTO :NEW.id_type FROM vc_nsi_276 WHERE code=:NEW.code_vc_nsi_276; "

		    + " IF :NEW.id_type = " + VocGisContractQualityLevelType.i.RANGE + " THEN BEGIN "
		    + "   :NEW.indicatorvalue    := NULL; "
		    + "   :NEW.indicatorvalue_is := 0; "
		    + "   IF :NEW.indicatorvalue_from IS NULL THEN "
		    + "      raise_application_error (-20000, 'Укажите начало диапазона показателя качества'); "
		    + "   END IF;"

		    + "   IF :NEW.id_type = 1 AND :NEW.indicatorvalue_to IS NULL THEN "
		    + "      raise_application_error (-20000, 'Укажите конец диапазона показателя качества'); "
		    + "   END IF;"

		    + "   IF :NEW.id_type = 1 AND :NEW.indicatorvalue_to <= :NEW.indicatorvalue_from THEN "
		    + "      raise_application_error (-20000, 'Укажите конец диапазона строго больше начала диапазона иного показателя качества'); "
		    + "   END IF;"
		    + " END; END IF; "

		    + " IF :NEW.id_type = " + VocGisContractQualityLevelType.i.NUMBER + " THEN BEGIN "
		    + "   :NEW.indicatorvalue_from := NULL; "
		    + "   :NEW.indicatorvalue_to   := NULL; "
		    + "   :NEW.indicatorvalue_is   := 0; "
		    + "   IF :NEW.indicatorvalue IS NULL THEN "
		    + "      raise_application_error (-20000, 'Укажите числовое значение показателя качества'); "
		    + "   END IF;"
		    + " END; END IF; "

		    + " IF :NEW.id_type = " + VocGisContractQualityLevelType.i.CORRESPOND + " THEN BEGIN "
		    + "   :NEW.indicatorvalue_from := NULL; "
		    + "   :NEW.indicatorvalue_to   := NULL; "
		    + "   :NEW.indicatorvalue      := NULL; "
		    + "   IF :NEW.indicatorvalue_is IS NULL THEN "
		    + "      :NEW.indicatorvalue_is := 0; "
		    + "   END IF;"
		    + " END; END IF; "

		    + "  IF :NEW.id_type <> " + VocGisContractQualityLevelType.i.CORRESPOND + " THEN BEGIN "
		    + "    SELECT code INTO :NEW.code_vc_okei FROM vc_okei WHERE code = :NEW.code_vc_okei;"
		    + "    EXCEPTION WHEN NO_DATA_FOUND THEN raise_application_error (-20000, 'Укажите единицу измерения показателя качества'); "
		    + "  END; END IF; "

                    + " FOR i IN ("
                        + "SELECT "
                        + " o.uuid "
			+ " , subj.uuid_sr_ctr_obj "
			+ " , sr_ctr.label sr_ctr_label "
                        + "FROM "
                        + " tb_sr_ctr_qls o "
			+ " LEFT JOIN tb_sr_ctr      sr_ctr  ON sr_ctr.uuid = o.uuid_sr_ctr "
			+ " LEFT JOIN tb_sr_ctr_subj subj    ON subj.uuid = o.uuid_sr_ctr_subj "
                        + "WHERE o.is_deleted = 0 "
                        + " AND o.uuid_sr_ctr = :NEW.uuid_sr_ctr "
                        + " AND o.code_vc_nsi_276 = :NEW.code_vc_nsi_276 "
                        + " AND o.uuid <> :NEW.uuid "
			+ " AND o.uuid_sr_ctr_subj = :NEW.uuid_sr_ctr_subj"
                    + ") LOOP"
                        + " raise_application_error (-20000, "
			+ "'Показатель качества ' "
			+ "|| ' уже есть ' || CASE WHEN i.uuid_sr_ctr_obj IS NULL THEN 'в предмете' ELSE 'в поставляемом ресурсе объекта жилищного фонда' END "
			+ "|| ' договора ' || i.sr_ctr_label "
			+ "|| ''); "
                    + " END LOOP; "

                + "END; END IF; "
        + "END;");
    }

    public static Map<String, Object> toMap(ExportSupplyResourceContractType.Quality q) {

	final Map<String, Object> r = DB.HASH();

	r.put(c.ADDITIONALINFORMATION.lc(), q.getAdditionalInformation());

	r.put(c.CODE_VC_NSI_276.lc(), q.getQualityIndicator().getCode());

	ExportSupplyResourceContractType.Quality.IndicatorValue val = q.getIndicatorValue().get(0);

	if (val.isCorrespond() != null) {
	    r.put(c.ID_TYPE.lc(), VocGisContractQualityLevelType.i.CORRESPOND.getId());
	    r.put(c.INDICATORVALUE_IS.lc(), DB.ok(val.isCorrespond())? 1 : 0);
	}

	if (DB.ok(val.getNumber())) {
	    r.put(c.ID_TYPE.lc(), VocGisContractQualityLevelType.i.NUMBER.getId());
	    r.put(c.INDICATORVALUE.lc(), val.getNumber());
	}

	if (DB.ok(val.getStartRange())) {
	    r.put(c.ID_TYPE.lc(), VocGisContractQualityLevelType.i.RANGE.getId());
	    r.put(c.INDICATORVALUE_FROM.lc(), val.getStartRange());
	    r.put(c.INDICATORVALUE_TO.lc(), val.getEndRange());
	}

	if(DB.ok(val.getOKEI())) {
	    r.put(c.CODE_VC_OKEI.lc(), val.getOKEI());
	}

	return r;
    }

    public static Map<String, Object> toMap(ExportSupplyResourceContractObjectAddressResultType.Quality q) {

	final Map<String, Object> r = DB.HASH();

	r.put(c.ADDITIONALINFORMATION.lc(), q.getAdditionalInformation());

	r.put(c.CODE_VC_NSI_276.lc(), q.getQualityIndicator().getCode());

	ExportSupplyResourceContractObjectAddressResultType.Quality.IndicatorValue val = q.getIndicatorValue().get(0);

	if (val.isCorrespond() != null) {
	    r.put(c.ID_TYPE.lc(), VocGisContractQualityLevelType.i.CORRESPOND.getId());
	    r.put(c.INDICATORVALUE_IS.lc(), DB.ok(val.isCorrespond())? 1 : 0);
	}

	if (DB.ok(val.getNumber())) {
	    r.put(c.ID_TYPE.lc(), VocGisContractQualityLevelType.i.NUMBER.getId());
	    r.put(c.INDICATORVALUE.lc(), val.getNumber());
	}

	if (DB.ok(val.getStartRange())) {
	    r.put(c.ID_TYPE.lc(), VocGisContractQualityLevelType.i.RANGE.getId());
	    r.put(c.INDICATORVALUE_FROM.lc(), val.getStartRange());
	    r.put(c.INDICATORVALUE_TO.lc(), val.getEndRange());
	}

	if(DB.ok(val.getOKEI())) {
	    r.put(c.CODE_VC_OKEI.lc(), val.getOKEI());
	}

	return r;
    }
}
