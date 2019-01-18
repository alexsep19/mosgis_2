package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.STRING;
import static ru.eludia.base.model.Type.UUID;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;

public class VocNsi236 extends Table {

    public enum c implements EnColEnum {

        F_D966DD6CBC          (UUID, null, "Вид коммунальной услуги"),
        GUID_VC_NSI_3         (UUID, new Virt("''||F_D966DD6CBC"), "Вид коммунальной услуги (синоним)"),

        F_ADEBB17EBE          (UUID, null, "Вид коммунального ресурса"),
        GUID_VC_NSI_239       (UUID, new Virt("''||F_ADEBB17EBE"), "Вид коммунального ресурса (синоним)"),

        F_C5E560ED97          (STRING, null, "Единица измерения"),
        UNIT                  (STRING, new Virt("''||F_C5E560ED97"), "Единица измерения (синоним)")
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

    public VocNsi236 () {

        super ("vc_nsi_236", "Справочник ГИС ЖКХ номер 236");

        cols  (c.class);
    }

}
