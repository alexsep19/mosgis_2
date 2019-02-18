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

public class VocNsi3 extends Table {

    public enum c implements EnColEnum {

	CODE                  (STRING, 20, "Код элемента справочника, уникальный в пределах справочника"),

	F_D966DD6CBC          (STRING, null, "Вид коммунальной услуги"),
	LABEL                 (STRING, new Virt("''||F_D966DD6CBC"), "Вид коммунальной услуги (синоним)"),

	GUID                  (UUID, "Глобально-уникальный идентификатор элемента справочника"),

	F_1B226CBFBE          (STRING, null, "Единица измерения мощности и присоединенной нагрузки"),

	F_C6E5A29665          (STRING, null, "Единица измерения"),

	F_C650537E54          (STRING, null, "Порядок сортировки"),

	F_1587117ECC          (UUID,   null, "Вид коммунального ресурса для ОКИ"),

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

    public VocNsi3 () {

        super ("vc_nsi_3", "Справочник ГИС ЖКХ номер 3: коммунальные услуги");

        cols  (c.class);

        pk    (c.GUID);
    }

    public static Select getVocSelect() throws SQLException {

	return ModelHolder.getModel()
	    .select(VocNsi3.class, "code AS id", "label")
	    .where(VocNsi3.c.ISACTUAL, 1)
	    .orderBy(VocNsi3.c.F_C650537E54);
    }
}
