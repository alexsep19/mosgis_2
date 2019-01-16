package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.DATE;
import static ru.eludia.base.model.Type.STRING;
import static ru.eludia.base.model.Type.NUMERIC;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Num;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;

public class SupplyResourceContractSubject extends EnTable {

    public enum c implements EnColEnum {

        UUID_SR_CTR           (SupplyResourceContract.class, "Договор"),

        CODE_VC_NSI_239       (STRING, 20, "Ссылка на НСИ \"Вид коммунальной услуги\" (реестровый номер 239)"),
        CODE_VC_NSI_3         (STRING, 20, "Ссылка на НСИ \"Вид коммунального ресурса\" (реестровый номер 3)"),

        SERVICETYPEREF        (STRING, new Virt ("(''||\"CODE_VC_NSI_3\")"),  "Вид коммунальной услуги"),
        MUNICIPALRESOURCEREF  (STRING, new Virt ("(''||\"CODE_VC_NSI_239\")"),  "Тарифицируемый ресурс"),

        STARTSUPPLYDATE       (DATE, "Дата начала поставки ресурса"),
        ENDSUPPLYDATE         (DATE, null, "Дата окончания поставки ресурса"),

        VOLUME                (NUMERIC, 30, 12, null, "Плановый объем"),
        UNIT                  (VocOkei.class, null, "Единица измерения"),
        FEEDINGMODE           (STRING, 250, null, "Режим подачи"),

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
    }

    public SupplyResourceContractSubject () {

        super ("tb_sr_ctr_subj", "Предмет договора РСО");

        cols  (c.class);

        key   ("uuid_sr_ctr", c.UUID_SR_CTR);

        trigger("BEFORE INSERT OR UPDATE", ""
                + "DECLARE"
                + " PRAGMA AUTONOMOUS_TRANSACTION; "
                + "BEGIN "

                + " IF :NEW.is_deleted = 0 THEN "

                    + " IF :NEW.startsupplydate > :NEW.endsupplydate "
                    + " THEN "
                    + "   raise_application_error (-20000, 'Дата начала поствки ресурса не может превышать дату окончания поставки ресурса. Операция отменена.'); "
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

                + " END IF; "

                + " IF :NEW.volume IS NOT NULL AND :NEW.volume < 0 "
                + " THEN "
                + "   raise_application_error (-20000, 'Укажите неотрицательный плановый объем. Операция отменена.'); "
                + " END IF; "

                + " IF :NEW.volume IS NOT NULL AND :NEW.unit IS NULL "
                + " THEN "
                + "   raise_application_error (-20000, 'Укажите, пожалуйста, единицу измерения планового объема. Операция отменена.'); "
                + " END IF; "
        + "END;");
    }

}
