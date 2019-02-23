package ru.eludia.products.mosgis.db.model.voc.nsi;

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

public class VocNsi38 extends EnTable {
    
    public enum c implements EnColEnum {
        
        CODE         (STRING, 20, "Код элемента справочника, уникальный в пределах справочника"),
        
        F_415A4B3285 (STRING, null, "Вид электростанции"),
        F_674707B1DD (STRING, null, "Вид тепловой электростанции"),
        
        LABEL        (STRING, new Virt ("DECODE (\"F_674707B1DD\", NULL, \"F_415A4B3285\", \"F_415A4B3285\"||' ('|| \"F_674707B1DD\" ||')')"), "Полное наименование вида электростанции"),
        
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
    
    public VocNsi38 () {

        super ("vc_nsi_38", "Справочник ГИС ЖКХ номер 38");

        cols  (c.class);
        
    }
    
    public static void addTo(DB db, JsonObjectBuilder job) throws SQLException {

        db.addJsonArrays(job,
            db.getModel()
                .select  (VocNsi38.class, "code AS id", "label")
                .where   (VocNsi38.c.ISACTUAL, 1)
                .orderBy (VocNsi38.c.CODE)
        );
        
    }
    
    public static Select getVocSelect() throws SQLException {
        return ModelHolder.getModel()
            .select  (VocNsi38.class, "code AS id", "label")
            .where   (VocNsi38.c.ISACTUAL, 1)
            .orderBy (VocNsi38.c.CODE);
    }
    
}
