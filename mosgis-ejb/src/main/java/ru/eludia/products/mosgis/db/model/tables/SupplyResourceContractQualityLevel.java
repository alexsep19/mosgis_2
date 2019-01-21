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
import ru.eludia.products.mosgis.db.model.voc.VocOkei;

public class SupplyResourceContractQualityLevel extends EnTable {

    public enum c implements EnColEnum {

        UUID_SR_CTR           (SupplyResourceContract.class, null, "Договор ресурсоснабжения (заполняется всегда)"),

        UUID_SR_CTR_SUBJ      (SupplyResourceContractSubject.class, null, "Предмет договора (заполняется если показатель привязан к предмету договора)"),

        CODE_VC_NSI_276       (STRING, 20, "Ссылка на НСИ \"Показатели качества коммунальных ресурсов\" (реестровый номер 276)"),

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
    }

    public SupplyResourceContractQualityLevel () {

        super ("tb_sr_ctr_qls", "Показатели качества договора РСО");

        cols  (c.class);

        key   ("uuid_sr_ctr_subj", c.UUID_SR_CTR_SUBJ);

        trigger("BEFORE INSERT OR UPDATE", ""
                + "DECLARE"
                + " PRAGMA AUTONOMOUS_TRANSACTION; "
                + "BEGIN "

                + "IF :NEW.is_deleted = 0 THEN BEGIN "

                    + "  IF :NEW.uuid_sr_ctr_subj IS NOT NULL THEN "
                    + "     SELECT uuid_sr_ctr INTO :NEW.uuid_sr_ctr FROM tb_sr_ctr_subj WHERE uuid=:NEW.uuid_sr_ctr_subj; "
                    + "  END IF;"

                    + " FOR i IN ("
                        + "SELECT "
                        + " o.uuid "
                        + "FROM "
                        + " tb_sr_ctr_qls o "
                        + "WHERE o.is_deleted = 0 "
                        + " AND o.uuid_sr_ctr = :NEW.uuid_sr_ctr "
                        + " AND o.code_vc_nsi_276 = :NEW.code_vc_nsi_276 "
                        + " AND o.uuid <> :NEW.uuid "
                    + ") LOOP"
                        + " raise_application_error (-20000, 'Указанный показатель уже есть в договоре. Операция отменена.'); "
                    + " END LOOP; "

                + "END; END IF; "
        + "END;");
    }

}
