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
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.ModelHolder;

public class VocNsi95 extends Table {

    public enum c implements EnColEnum {

	CODE                  (STRING, 20, "Код элемента справочника, уникальный в пределах справочника"),

	F_A17F8DD862          (STRING, null, "Вид документа, удостоверяющего личность"),
	LABEL                 (STRING, new Virt("''||F_A17F8DD862"), "Вид документа, удостоверяющего личность (синоним)"),

	GUID                  (UUID, "Глобально-уникальный идентификатор элемента справочника"),

	ISACTUAL              (BOOLEAN, "Признак актуальности элемента справочника")
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

    public VocNsi95 () {

        super ("vc_nsi_95", "Справочник ГИС ЖКХ номер 95: документ, удостоверяющий личность");

        cols  (c.class);

        pk    (c.GUID);
    }
}
