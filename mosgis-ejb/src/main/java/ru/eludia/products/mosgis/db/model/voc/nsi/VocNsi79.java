package ru.eludia.products.mosgis.db.model.voc.nsi;

import java.sql.SQLException;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.BOOLEAN;
import static ru.eludia.base.model.Type.STRING;
import static ru.eludia.base.model.Type.UUID;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocNsi79Ref80;

public class VocNsi79 extends EnTable {
    
    public enum c implements EnColEnum {

	CODE                  (STRING, 20, "Код элемента справочника, уникальный в пределах справочника"),

	F_BCE3C198BF          (STRING, null, "Вид документа"),
        F_F7795FCFB3          (BOOLEAN, null, "Наличие номера и даты документа"),

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

    public VocNsi79 () {

        super ("vc_nsi_79", "Справочник ГИС ЖКХ номер 79: Вид документа программы");

        cols  (c.class);

        pk    (c.GUID);
    }
    
    public static void addToOverhaulRegionalProgramDocument (DB db, JsonObjectBuilder job) throws SQLException {

        db.addJsonArrays (job,
            db.getModel ()
                .select    (VocNsi79Ref80.class, "AS vc_nsi_79")
                .toOne     (Nsi79.class, "label AS label", "id AS id")
                    .on    ("vc_nsi_79.guid_from = vw_nsi_79.guid")
                .toOne     ("vc_nsi_80")
                    .where ("code", 2)
                    .on    ("vc_nsi_79.guid_to = vc_nsi_80.guid")
                .orderBy   (Nsi79.c.ID)
        );
        
    }
    
    public static void addToOverhaulShortProgramDocument (DB db, JsonObjectBuilder job) throws SQLException {
        
        db.addJsonArrays (job,
            db.getModel ()
                .select    (VocNsi79Ref80.class, "AS vc_nsi_79")
                .toOne     (Nsi79.class, "label AS label", "id AS id")
                    .on    ("vc_nsi_79.guid_from = vw_nsi_79.guid")
                .toOne     ("vc_nsi_80")
                    .where ("code", 3)
                    .on    ("vc_nsi_79.guid_to = vc_nsi_80.guid")
                .orderBy   (Nsi79.c.ID)
        );
        
    }
    
}
