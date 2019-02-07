package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.BOOLEAN;
import static ru.eludia.base.model.Type.STRING;
import static ru.eludia.base.model.Type.UUID;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.ejb.ModelHolder;

public class VocNsi33 extends Table {

    public enum c implements EnColEnum {
        
        CODE                  (STRING, 20, "Код элемента справочника, уникальный в пределах справочника"),

        F_C8E745BC63          (UUID, null, "Вид объекта"),
        LABEL                 (UUID, new Virt ("''||F_C8E745BC63"), "Вид объекта (синоним)"),

        F_995A303098          (UUID, null, "Тип объекта"),
        TYPE                  (UUID, new Virt ("''||F_995A303098"), "Тип объекта (синоним)"),
        
        IS_OBJECT             (BOOLEAN, new Virt("DECODE (\"LABEL\", 'Объект', 1, 0)"), "1, если является объектом, иначе 0"),
        
        GUID                  (UUID, "Глобально-уникальный идентификатор элемента справочника"),
        
        ISACTUAL              (BOOLEAN, "Актуально")
        
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

    public VocNsi33 () {

        super ("vc_nsi_33", "Справочник ГИС ЖКХ номер 33");

        cols  (c.class);
        
        pk    (c.CODE);
    }
    
    public static Select getVocSelect() throws SQLException {
        return ModelHolder.getModel()
            .select  (VocNsi33.class, "code AS id", "label", "type")
            .where   (VocNsi33.c.ISACTUAL, 1)
            .orderBy (VocNsi33.c.CODE);
    }

}
