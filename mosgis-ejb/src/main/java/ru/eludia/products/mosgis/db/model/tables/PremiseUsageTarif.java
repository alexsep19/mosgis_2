package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class PremiseUsageTarif extends EnTable  {

    public static final String TABLE_NAME = "tb_pu_tfs";

    public enum c implements EnColEnum {
	UUID_ORG               (VocOrganization.class, "Организация, которая завела данный тариф в БД"),

	NAME                   (Type.STRING, 4000, null, "Наименование"),

	DATEFROM               (Type.DATE, "Дата начала действия"),
	DATETO                 (Type.DATE, null, "Дата окончания действия"),

	PRICE                  (Type.NUMERIC, 15, 3, null, "Величина"),

	ID_CTR_STATUS          (VocGisStatus.class,    VocGisStatus.DEFAULT,    "Статус с точки зрения mosgis"),
	ID_CTR_STATUS_GIS      (VocGisStatus.class,    VocGisStatus.DEFAULT,    "Статус с точки зрения ГИС ЖКХ"),

	ID_LOG                 (PremiseUsageTarifLog.class, "Последнее событие редактирования")
        ;

        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        @Override
        public boolean isLoggable () {
            switch (this) {
                case UUID_ORG:
                case ID_LOG:
                    return false;
                default:
                    return true;
            }
        }

    }

    public PremiseUsageTarif () {

	super  (TABLE_NAME, "Тарифы: размер платы за пользование жилым помещением");

	cols   (c.class);

	key    (c.UUID_ORG);

        trigger ("BEFORE UPDATE", ""
	    + "DECLARE "
	    + "BEGIN "
	    + "IF :NEW.ID_CTR_STATUS <> :OLD.ID_CTR_STATUS "
	    + " AND :OLD.ID_CTR_STATUS <> " + VocGisStatus.i.FAILED_PLACING.getId()
	    + " AND :NEW.ID_CTR_STATUS =  " + VocGisStatus.i.PROJECT.getId()
	    + " THEN "
	    + " :NEW.ID_CTR_STATUS := " + VocGisStatus.i.MUTATING.getId()
	    + "; END IF; "
        + "END;");

	trigger("BEFORE INSERT OR UPDATE", ""
	    + "DECLARE"
	    + " PRAGMA AUTONOMOUS_TRANSACTION; "
	    + "BEGIN "
	    + " IF :NEW.is_deleted = 0 THEN BEGIN "
		+ " FOR i IN ("
		    + "SELECT "
		    + " o.name     label "
		    + " , o.datefrom dt_from "
		    + " , o.dateto   dt_to "
		    + "FROM "
		    + TABLE_NAME + " o "
		    + "WHERE o.is_deleted = 0 "
		    + " AND o.uuid <> :NEW.uuid "
		    + " AND (o.datefrom <= :NEW.dateto   OR :NEW.dateto IS NULL) "
		    + " AND (o.dateto   >= :NEW.datefrom OR o.dateto IS NULL) "
		+ ") LOOP"
		    + " raise_application_error (-20000, "
		    + "'Указанный период пересекается с другой информацией о размере платы за пользование жилым помещением ' || i.label "
		    + "|| ' с ' "
		    + "|| TO_CHAR (i.dt_from, 'DD.MM.YYYY')"
		    + "|| CASE WHEN i.dt_to IS NOT NULL THEN ' по ' || TO_CHAR (i.dt_to, 'DD.MM.YYYY') ELSE '' END "
		    + "); "
		+ " END LOOP; "
	    + " END; END IF; "
	    + " COMMIT; "
	    + "END;");

	trigger ("AFTER UPDATE", ""
	    + "DECLARE "
	    + " PRAGMA AUTONOMOUS_TRANSACTION; "
	    + "BEGIN "
	    + " IF :NEW.price <> :OLD.price THEN "
	    + "   UPDATE " + TarifCoeff.TABLE_NAME + " c SET price = c.coefficientvalue * :NEW.price WHERE uuid_tf = :NEW.uuid; "
	    + "   COMMIT; "
	    + " END IF; "
        + "END;");
    }
}