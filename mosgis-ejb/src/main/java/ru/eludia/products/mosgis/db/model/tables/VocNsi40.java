package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.BOOLEAN;
import static ru.eludia.base.model.Type.STRING;
import static ru.eludia.base.model.Type.UUID;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.ejb.ModelHolder;

public class VocNsi40 extends EnTable {
    
    public enum c implements EnColEnum {
        
        CODE         (STRING, 20, "Код элемента справочника, уникальный в пределах справочника"),
        
        F_8BB30AD91A (STRING, null, "Вид топлива для бытовых нужд"),
        F_0EF1925149 (STRING, null, "Наименование топлива"),
        
        LABEL        (STRING, new Virt ("DECODE (\"F_0EF1925149\", NULL, \"F_8BB30AD91A\", \"F_8BB30AD91A\"||' ('|| \"F_0EF1925149\" ||')')"), "Полное наименование топлива"),
        
        GUID         (UUID, "Глобально-уникальный идентификатор элемента справочника"),
        
        ISACTUAL     (BOOLEAN, "Актуально")
        
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
    
    public VocNsi40 () {

        super ("vc_nsi_40", "Справочник ГИС ЖКХ номер 40");

        cols  (c.class);
        
    }
    
    public static void addTo(DB db, JsonObjectBuilder job) throws SQLException {

        db.addJsonArrays(job,
            db.getModel()
                .select  (VocNsi40.class, "code AS id", "label")
                .where   (VocNsi40.c.ISACTUAL, 1)
                .orderBy (VocNsi40.c.CODE)
        );
        
    }
    
    public static Select getVocSelect() throws SQLException {
        return ModelHolder.getModel()
            .select  (VocNsi40.class, "code AS id", "label")
            .where   (VocNsi40.c.ISACTUAL, 1)
            .orderBy (VocNsi40.c.CODE);
    }
    
}
