package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.DATETIME;
import static ru.eludia.base.model.Type.STRING;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;

public class Interval extends EnTable {

    public enum c implements EnColEnum {

	UUID_SR_CTR           (SupplyResourceContract.class, "Договор"),

	CODE_VC_NSI_3         (STRING, 20, "Ссылка на НСИ \"Вид коммунального ресурса\" (реестровый номер 3)"),
	CODE_VC_NSI_239       (STRING, 20, "Ссылка на НСИ \"Вид коммунальной услуги\" (реестровый номер 239)"),

	MUNICIPALSERVICE      (STRING, new Virt ("(''||\"CODE_VC_NSI_3\")"),  "Вид коммунальной услуги"),
	RATEDRESOURCE         (STRING, new Virt ("(''||\"CODE_VC_NSI_239\")"),  "Вид тарифицируемого ресурса"),

	STARTDATEANDTIME      (DATETIME, "Дата и время начала"),
	ENDDATEANDTIME        (DATETIME, null, "Дата и время окончания"),

	INTERVALREASON        (STRING, 1000, null, "Причина"),

	ID_LOG                (IntervalLog.class, null, "Последнее событие редактирования"),

	INTERVALGUID          (Type.UUID, null, "Идентификатор информации о перерывах в ГИС ЖКХ"),
	ID_CTR_STATUS         (VocGisStatus.class, VocGisStatus.DEFAULT, "Статус с точки зрения mosgis"),
	ID_CTR_STATUS_GIS     (VocGisStatus.class, VocGisStatus.DEFAULT, "Статус с точки зрения ГИС ЖКХ")
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

    public Interval () {

        super ("tb_intervals", "Информация о перерывах РСО");

        cols  (c.class);

        key   ("uuid_sr_ctr", c.UUID_SR_CTR);

        trigger("BEFORE INSERT OR UPDATE", ""
                + "DECLARE "
//                + " PRAGMA AUTONOMOUS_TRANSACTION; "
                + "BEGIN "

                + " IF :NEW.is_deleted = 0 THEN BEGIN "

                    + " IF :NEW.startdateandtime > :NEW.enddateandtime"
                    + " THEN "
                    + "   raise_application_error (-20000, 'Дата начала перерыва не может превышать дату окончания перерыва. Операция отменена.'); "
                    + " END IF; "

	    + " END; END IF; " // IF :NEW.is_deleted = 0
        + "END;");
    }

}
