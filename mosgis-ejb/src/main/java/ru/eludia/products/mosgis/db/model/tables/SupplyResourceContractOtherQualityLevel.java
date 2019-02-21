package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.STRING;
import static ru.eludia.base.model.Type.NUMERIC;
import static ru.eludia.base.model.Type.INTEGER;
import static ru.eludia.base.model.Type.BOOLEAN;
import static ru.eludia.base.model.def.Bool.FALSE;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocGisContractQualityLevelType;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;

public class SupplyResourceContractOtherQualityLevel extends EnTable {

    public enum c implements EnColEnum {

        UUID_SR_CTR           (SupplyResourceContract.class, null, "Договор ресурсоснабжения (заполняется всегда)"),

        UUID_SR_CTR_SUBJ      (SupplyResourceContractSubject.class, null, "Предмет договора, поставляемый ресурс ОЖФ"),

	CODE_VC_NSI_3         (STRING, 20, null, "Ссылка на НСИ \"Вид коммунальной услуги\" (реестровый номер 3)"),
	CODE_VC_NSI_239       (STRING, 20, null, "Ссылка на НСИ \"Вид коммунального ресурса\" (реестровый номер 239)"),

        LABEL                 (STRING, null, "Наименование иного показателя качества"),
        ID_TYPE               (VocGisContractQualityLevelType.class, "Тип иного показателя качества"),

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
    }

    public SupplyResourceContractOtherQualityLevel () {

        super ("tb_sr_ctr_other_qls", "Иные качества договора РСО");

        cols  (c.class);

        key   ("uuid_sr_ctr_subj", c.UUID_SR_CTR_SUBJ);

        trigger("BEFORE INSERT OR UPDATE", ""
                + "DECLARE"
                + " PRAGMA AUTONOMOUS_TRANSACTION; "
                + "BEGIN "

                + "IF :NEW.is_deleted = 0 THEN BEGIN "

		    + "  IF :NEW.uuid_sr_ctr_subj IS NULL THEN "
		    + "     raise_application_error (-20000, 'Иной показатель качества должен быть привязан к предмету договора или к поставляемому ресурсу ОЖФ. Операция отменена.'); "
		    + "  END IF;"

		    + "  SELECT uuid_sr_ctr INTO :NEW.uuid_sr_ctr FROM tb_sr_ctr_subj WHERE uuid=:NEW.uuid_sr_ctr_subj; "
		    + "  SELECT code_vc_nsi_3   INTO :NEW.code_vc_nsi_3   FROM tb_sr_ctr_subj WHERE uuid=:NEW.uuid_sr_ctr_subj; "
		    + "  SELECT code_vc_nsi_239 INTO :NEW.code_vc_nsi_239 FROM tb_sr_ctr_subj WHERE uuid=:NEW.uuid_sr_ctr_subj; "

                    + "  IF :NEW.id_type = 1 AND :NEW.indicatorvalue_from IS NULL THEN "
                    + "     raise_application_error (-20000, 'Укажите начало диапазона иного показателя качества. Операция отменена.'); "
                    + "  END IF;"

		    + "  IF :NEW.id_type = 1 AND :NEW.indicatorvalue_to IS NULL THEN "
		    + "     raise_application_error (-20000, 'Укажите конец диапазона иного показателя качества. Операция отменена.'); "
		    + "  END IF;"

                    + "  IF :NEW.id_type = 2 AND :NEW.indicatorvalue IS NULL THEN "
                    + "     raise_application_error (-20000, 'Укажите значение иного показателя качества. Операция отменена.'); "
                    + "  END IF;"

		    + "  IF :NEW.code_vc_nsi_3 IS NULL THEN "
		    + "     raise_application_error (-20000, 'Укажите вид коммунальной услуги.'); "
		    + "  END IF;"

		    + "  IF :NEW.code_vc_nsi_239 IS NULL THEN "
		    + "     raise_application_error (-20000, 'Укажите коммунальный ресурс.'); "
		    + "  END IF;"

                    + "  IF :NEW.id_type = 1 AND :NEW.code_vc_okei IS NULL THEN "
                    + "     raise_application_error (-20000, 'Укажите единицу измерения иного показателя качества. Операция отменена.'); "
                    + "  END IF;"

                    + " FOR i IN ("
                        + "SELECT "
                        + " o.uuid "
                        + " , o.label "
			+ " , subj.uuid_sr_ctr_obj "
                        + " , sr_ctr.label sr_ctr_label "
                        + "FROM "
                        + " tb_sr_ctr_other_qls o "
                        + " LEFT JOIN tb_sr_ctr_subj subj    ON subj.uuid = o.uuid_sr_ctr_subj "
                        + " LEFT JOIN tb_sr_ctr      sr_ctr  ON sr_ctr.uuid = o.uuid_sr_ctr "
                        + "WHERE o.is_deleted = 0 "
                        + " AND o.uuid_sr_ctr = :NEW.uuid_sr_ctr "
                        + " AND o.label = :NEW.label "
                        + " AND subj.is_deleted = 0 "
                        + " AND o.uuid <> :NEW.uuid "
			+ " AND o.uuid_sr_ctr_subj = :NEW.uuid_sr_ctr_subj"
                    + ") LOOP"
                        + " raise_application_error (-20000, "
                        + "'Иной показатель качества ' || i.label "
                        + "|| ' уже есть ' "
			+ "|| CASE WHEN i.uuid_sr_ctr_obj IS NULL THEN 'в предмете' ELSE 'в поставляемом ресурсе объекта жилищного фонда' END "
			+ "|| ' договора ' || i.sr_ctr_label"
                        + "|| '. Операция отменена.'); "
                    + " END LOOP; "

                + "END; END IF; "
        + "END;");
    }

}
