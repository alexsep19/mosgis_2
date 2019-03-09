package ru.eludia.products.mosgis.db.model.voc.nsi;

import java.sql.SQLException;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.DB;
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

public class VocNsi276 extends Table {

    public enum c implements EnColEnum {

        CODE                  (STRING, 20, "Код элемента справочника, уникальный в пределах справочника"),
	GUID                  (UUID, "Глобально-уникальный идентификатор элемента справочника"),

        F_29FD5CCB75          (STRING, null, "Наименование"),
        LABEL                 (STRING, new Virt("''||F_29FD5CCB75"), "Наименование (синоним)"),

        F_3D99967FD6          (STRING, null, "Тип показателя"),
        TYPE_LABEL            (STRING, new Virt("''||F_3D99967FD6"), "Тип показателя (синоним)"),
        ID_TYPE               (STRING, new Virt("CASE WHEN F_3D99967FD6 = 'Диапазон' THEN 1 WHEN F_3D99967FD6 = 'Число' THEN 2 WHEN F_3D99967FD6 = 'Логическое' THEN 3 ELSE NULL END"), "Код типа показателя (синоним): 1 - диапазон, 2 - число, 3 - логическое"),

        F_FC5329FE66          (UUID, null, "Ресурс к которому относится показатель"),
        GUID_VC_NSI_239       (UUID, new Virt("''||F_FC5329FE66"), "Ресурс к которому относится показатель (синоним)"),

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

    public VocNsi276 () {

        super ("vc_nsi_276", "Справочник ГИС ЖКХ номер 276");

        cols  (c.class);

        pk    (c.GUID);
    }

    public static void addTo(DB db, JsonObjectBuilder job) throws SQLException {

        db.addJsonArrays(job,
            db.getModel()
                .select(VocNsi276.class, "code AS id", "label", "id_type")
                .toMaybeOne(VocNsi239.class, "code").on("vc_nsi_276.guid_vc_nsi_239 = vc_nsi_239.guid")
                .where(VocNsi276.c.ISACTUAL, 1)
                .orderBy("vc_nsi_276.label")
        );
    }

    public static Select getVocSelect() throws SQLException {
        return ModelHolder.getModel()
            .select(VocNsi276.class, "code AS id", "label", "id_type")
            .toMaybeOne(VocNsi239.class, "code").on("vc_nsi_276.guid_vc_nsi_239 = vc_nsi_239.guid")
            .where(VocNsi276.c.ISACTUAL, 1)
            .orderBy("vc_nsi_276.label");
    }
}
