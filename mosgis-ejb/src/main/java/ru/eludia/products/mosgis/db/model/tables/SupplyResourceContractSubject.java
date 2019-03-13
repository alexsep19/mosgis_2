package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.DATE;
import static ru.eludia.base.model.Type.STRING;
import static ru.eludia.base.model.Type.NUMERIC;
import static ru.eludia.base.model.Type.BOOLEAN;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Num;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.voc.VocGisContractDimension;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;

public class SupplyResourceContractSubject extends EnTable {

    public enum c implements EnColEnum {
        UUID_XL               (InXlFile.class, "Файл импорта"),

        UUID_SR_CTR           (SupplyResourceContract.class, "Договор"),
	UUID_SR_CTR_OBJ       (SupplyResourceContractObject.class, null, "Объект жилищного фонда (IS NULL, если предмет договора, заполняется, если поставляемый ресурс ОЖФ)"),

	CODE_VC_NSI_3         (STRING, 20, "Ссылка на НСИ \"Вид коммунальной услуги\" (реестровый номер 3)"),
	CODE_VC_NSI_239       (STRING, 20, "Ссылка на НСИ \"Вид коммунального ресурса\" (реестровый номер 239)"),

        SERVICETYPEREF        (STRING, new Virt ("(''||\"CODE_VC_NSI_3\")"),  "Вид коммунальной услуги"),
        MUNICIPALRESOURCEREF  (STRING, new Virt ("(''||\"CODE_VC_NSI_239\")"),  "Тарифицируемый ресурс"),

        STARTSUPPLYDATE       (DATE, "Дата начала поставки ресурса"),
        ENDSUPPLYDATE         (DATE, null, "Дата окончания поставки ресурса"),

        VOLUME                (NUMERIC, 30, 12, null, "Плановый объем"),
        UNIT                  (VocOkei.class, null, "Единица измерения"),
        FEEDINGMODE           (STRING, 250, null, "Режим подачи"),

	// Заполняется в ОЖФ для отдельных ресурсов
	IS_HEAT_OPEN          (BOOLEAN, null, "Тип системы теплоснабжения: 1 - если открытая, 0 - если закрытая"),
	IS_HEAT_CENTRALIZED   (BOOLEAN, null, "Вид системы системы теплоснабжения: 1 - если централизованная, 0 - не централизованная"),

        ID_LOG                (SupplyResourceContractSubjectLog.class, null, "Последнее событие редактирования")
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
            switch (this) {
                case UUID_SR_CTR:
                case ID_LOG:
                    return false;
                default:
                    return true;
            }
        }

	public boolean isToXlImport() {

	    switch (this) {
		case UUID_XL:
		case CODE_VC_NSI_3:
		case CODE_VC_NSI_239:
		case STARTSUPPLYDATE:
		case ENDSUPPLYDATE:
		case IS_HEAT_OPEN:
		case IS_HEAT_CENTRALIZED:
		case VOLUME:
		case UNIT:
		case FEEDINGMODE:
		    return true;
	    default:
		return false;
	    }
	}

	public boolean isToXlImportVolume() {

	    switch (this) {
	    case UUID_XL:
	    case CODE_VC_NSI_3:
	    case CODE_VC_NSI_239:
	    case VOLUME:
	    case UNIT:
	    case FEEDINGMODE:
		return true;
	    default:
		return false;
	    }
	}
    }

    public SupplyResourceContractSubject () {

        super ("tb_sr_ctr_subj", "Предмет, поставляемый ресурс ОЖФ договора РСО");

        cols  (c.class);

        key   ("uuid_sr_ctr", c.UUID_SR_CTR);
	key   ("code_vc_nsi_239", c.CODE_VC_NSI_239, c.UUID_SR_CTR);

        trigger("BEFORE INSERT OR UPDATE", ""
                + "DECLARE"
                + " PRAGMA AUTONOMOUS_TRANSACTION; "
                + " ctr_effectivedate  DATE := NULL; "
                + " ctr_completiondate DATE := NULL; "
		+ " volume_type NUMBER := NULL; "
		+ " is_volume   NUMBER := NULL; "
                + "BEGIN "

                + " IF :NEW.is_deleted = 0 THEN BEGIN "

		    + " IF :NEW.uuid_sr_ctr IS NULL THEN "
		    + "   raise_application_error (-20000, 'Не удалось определить договор'); "
		    + " END IF; "

                    + " IF :NEW.startsupplydate > :NEW.endsupplydate "
                    + " THEN "
                    + "   raise_application_error (-20000, 'Дата начала поствки ресурса не может превышать дату окончания поставки ресурса. Операция отменена.'); "
                    + " END IF; "

                    + " SELECT effectivedate INTO ctr_effectivedate FROM tb_sr_ctr WHERE uuid = :NEW.uuid_sr_ctr "
                    + " ; IF :NEW.startsupplydate < ctr_effectivedate THEN "
                    + "   raise_application_error (-20000, 'Дата начала поставки ресурса должна быть больше или равна дате вступления договора в силу. Операция отменена.'); "
                    + " END IF; "

                    + " SELECT completiondate INTO ctr_completiondate FROM tb_sr_ctr WHERE uuid = :NEW.uuid_sr_ctr "
                    + " ; IF :NEW.endsupplydate > ctr_completiondate THEN "
                    + "   raise_application_error (-20000, 'Дата окончания поставки ресурсов должна быть меньше или равна дате окончания действия договора. Операция отменена.'); "
                    + " END IF; "

                    + " FOR i IN ("
                        + "SELECT "
                        + " o.startsupplydate "
                        + " , o.endsupplydate "
                        + " , nsi_3.f_d966dd6cbc   nsi_3_label "
                        + " , nsi_239.f_adebb17ebe nsi_239_label "
                        + "FROM "
                        + " tb_sr_ctr_subj o "
                        + " INNER JOIN vc_nsi_3   nsi_3   ON o.code_vc_nsi_3   = nsi_3.code "
                        + " INNER JOIN vc_nsi_239 nsi_239 ON o.code_vc_nsi_239 = nsi_239.code "
                        + "WHERE o.is_deleted = 0 "
                        + " AND o.uuid <> :NEW.uuid "
                        + " AND o.uuid_sr_ctr     = :NEW.uuid_sr_ctr "
			+ " AND NVL(o.uuid_sr_ctr_obj, '00') = NVL(:NEW.uuid_sr_ctr_obj, '00') "
                        + " AND o.code_vc_nsi_3   = :NEW.code_vc_nsi_3 "
                        + " AND o.code_vc_nsi_239 = :NEW.code_vc_nsi_239 "
                        + " AND (o.endsupplydate   >= :NEW.startsupplydate OR o.endsupplydate IS NULL) "
                        + " AND (o.startsupplydate <= :NEW.endsupplydate   OR :NEW.endsupplydate IS NULL) "
                        + ") LOOP"
                    + " raise_application_error (-20000, "
                        + "'Коммунальная услуга ' || i.nsi_239_label "
                        + "|| ' (коммунальный ресурс ' || i.nsi_3_label || ') уже поставляется с ' "
                        + "|| TO_CHAR (i.startsupplydate, 'DD.MM.YYYY') "
                        + "|| CASE WHEN i.endsupplydate IS NULL THEN NULL ELSE ' по ' "
                        + "|| TO_CHAR (i.endsupplydate, 'DD.MM.YYYY') END "
                        + "|| '. Операция отменена.'); "
                    + " END LOOP; "

		    + " IF :NEW.volume IS NULL THEN "
		    + "   :NEW.unit := NULL; "
		    + "   :NEW.feedingmode := NULL; "
		    + " END IF; "

		    + " IF :NEW.volume IS NOT NULL THEN "

		    + "   SELECT plannedvolumetype, isplannedvolume INTO volume_type, is_volume FROM tb_sr_ctr WHERE uuid = :NEW.uuid_sr_ctr; "

		    + "   IF is_volume = 0 THEN "
		    + "     raise_application_error (-20000, 'В договоре не указано наличие планового объема и режим подачи'); "
		    + "   END IF; "

		    + "   IF volume_type = " + VocGisContractDimension.i.BY_CONTRACT
		    + "     AND :NEW.uuid_sr_ctr_obj IS NOT NULL "
		    + "   THEN "
		    + "     raise_application_error (-20000, 'Укажите плановый объем и режим подачи в предмете договора, а не в поставляемом ресурсе'); "
		    + "   END IF; "

		    + "   IF volume_type = " + VocGisContractDimension.i.BY_HOUSE
		    + "     AND :NEW.uuid_sr_ctr_obj IS NULL "
		    + "   THEN "
		    + "     raise_application_error (-20000, 'Укажите плановый объем и режим подачи в поставляемом ресурсе, а не в предмете договора'); "
		    + "   END IF; "

		    + "   IF :NEW.volume < 0 THEN "
		    + "     raise_application_error (-20000, 'Укажите неотрицательный плановый объем'); "
		    + "   END IF; "

		    + "   IF :NEW.unit IS NULL THEN "
		    + "     raise_application_error (-20000, 'Укажите, пожалуйста, единицу измерения планового объема'); "
		    + "   END IF; "

		    + " END IF; " // IF :NEW.volume IS NOT NULL

                    + " IF UPDATING AND :NEW.code_vc_nsi_239 <> :OLD.code_vc_nsi_239 "
                    + " THEN BEGIN "
                    + "   FOR i IN ("
                    + "   SELECT "
                    + "    o.label "
                    + "   FROM "
                    + "    tb_sr_ctr_other_qls o "
                    + "   WHERE o.is_deleted = 0 "
                    + "    AND o.uuid_sr_ctr_subj = :NEW.uuid "
                    + "   ) LOOP"
                    + "    raise_application_error (-20000, 'Чтобы поменять коммунальный ресурс удалите иные показатели качества. Операция отменена.'); "
                    + "   END LOOP; "
                    + "   UPDATE tb_sr_ctr_qls SET is_deleted = 1 WHERE uuid_sr_ctr_subj = :NEW.uuid; "
                    + "   COMMIT; "
                    + " END; END IF; "

                + " END; END IF; " // IF :NEW.is_deleted = 0
        + "END;");
    }

}
