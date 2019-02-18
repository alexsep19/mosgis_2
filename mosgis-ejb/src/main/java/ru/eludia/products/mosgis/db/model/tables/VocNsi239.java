package ru.eludia.products.mosgis.db.model.tables;

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
import ru.eludia.products.mosgis.ejb.ModelHolder;

public class VocNsi239 extends Table {

    public static final String CODE_VC_NSI_239_HOT_WATER = "3";
    public static final String CODE_VC_NSI_239_HEAT_ENERGY = "4";

    public enum c implements EnColEnum {

        CODE                  (STRING, 20, "Код элемента справочника, уникальный в пределах справочника"),

        F_ADEBB17EBE          (STRING, null, "Тарифицируемый ресурс"),
        LABEL                 (STRING, new Virt("''||F_ADEBB17EBE"), "Наименование (синоним)"),

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

    public VocNsi239 () {

        super ("vc_nsi_239", "Справочник ГИС ЖКХ номер 239");

        cols  (c.class);

        pk    (c.GUID);
    }

    public static Select getVocSelect() throws SQLException {

	return ModelHolder.getModel()
	    .select(VocNsi239.class, "code AS id", "label")
	    .where(VocNsi239.c.ISACTUAL, 1)
	    .orderBy(VocNsi239.c.LABEL);
    }
}
