package ru.eludia.products.mosgis.db.model.voc;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.BOOLEAN;
import static ru.eludia.base.model.Type.INTEGER;
import static ru.eludia.base.model.Type.STRING;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;

public class VocOrganizationHours extends EnTable {

    public enum c implements EnColEnum {

        UUID_ORG             (VocOrganization.class,                "Ссылка на юридическое лицо"),
        WEEKDAY              (INTEGER,                              "День недели 0-6"),
        OPEN_FROM            (STRING,                        null,  "Режим работы с"),
        OPEN_TO              (STRING,                        null,  "Режим работы по"),
        BREAK_FROM           (STRING,                        null,  "Перерыв с"),
        BREAK_TO             (STRING,                        null,  "Перерыв по"),
        RECEPTION_FROM       (STRING,                        null,  "Часы приема с"),
        RECEPTION_TO         (STRING,                        null,  "Часы приема по"),
        IS_HOLIDAY           (BOOLEAN,                       0,     "Выходной"),
        NOTE                 (STRING, 1000,                  null,  "Комментарий"),
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
            return false;
        }

    }

    public VocOrganizationHours () {

        super ("vc_org_hours", "Режим работы юридического лица");

        cols   (c.class);

        key    ("uuid_org", c.UUID_ORG);

        trigger("BEFORE INSERT OR UPDATE", "BEGIN "
                + "IF :NEW.is_holiday = 1 THEN "
                + ":NEW.open_from      := NULL; "
                + ":NEW.open_to        := NULL; "
                + ":NEW.break_from     := NULL; "
                + ":NEW.break_to       := NULL; "
                + ":NEW.reception_from := NULL; "
                + ":NEW.reception_to   := NULL; "
                + "END IF; "
         + "END;");
    }
}
