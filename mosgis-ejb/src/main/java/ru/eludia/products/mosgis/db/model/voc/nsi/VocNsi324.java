package ru.eludia.products.mosgis.db.model.voc.nsi;

import java.sql.SQLException;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.STRING;
import static ru.eludia.base.model.Type.BOOLEAN;
import static ru.eludia.base.model.Type.UUID;
import ru.eludia.base.model.def.Bool;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.ModelHolder;

public class VocNsi324 extends Table {

    public enum c implements EnColEnum {

	CODE                  (STRING, 20, "Код элемента справочника, уникальный в пределах справочника"),

	F_BCE3C198BF          (STRING, null, "Вид документа"),
	LABEL                 (STRING, new Virt("''||F_BCE3C198BF"), "Вид документа"),

	GUID                  (UUID, "Глобально-уникальный идентификатор элемента справочника"),

	F_6C1D96410E          (STRING, null, "Сфера действия документа"),

	ISACTUAL              (Type.BOOLEAN, Bool.TRUE, "Признак актуальности элемента справочника")
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

    public VocNsi324 () {

        super ("vc_nsi_324", "Справочник ГИС ЖКХ номер 324: Вид закона и нормативного акта");

        cols  (c.class);

        pk    (c.GUID);
    }
}
