package ru.eludia.products.mosgis.db.model.voc.nsi;

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
	CODE                  (STRING, 20, "Код элемента справочника, уникальный в пределах справочника"),

	PARENT                (UUID, "Ссылка на родительскую запись"),

	GUID                  (UUID, "Уникальный идентификатор элемента справочника"),

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

	// HACK: 236 pkey
	pk    (c.GUID);
	pk    (c.PARENT);

	key   ("parent", c.PARENT);

	trigger("BEFORE INSERT OR UPDATE", ""
	    + "BEGIN "
	    + " IF :NEW.parent IS NULL THEN "
	    + "   :NEW.parent:= HEXTORAW(REPLACE(UPPER('00000000-0000-0000-0000-000000000000'), '-', '')); "
	    + " END IF; "
	    + "END;"
	);
    }
}
