package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;
import static ru.eludia.products.mosgis.db.model.voc.VocPassportFields.PASSPORT_FIELDS_LIST_NSI_REGISTRY_NUMBER;

public class VocRdCol extends Table {
    
    public static final String RD_NSI_REF_COL_NAME = "code_vc_nsi_" + PASSPORT_FIELDS_LIST_NSI_REGISTRY_NUMBER;
    
    public VocRdCol () {
        
        super ("vc_rd_cols", "Расширенные поля справочников ГИС РД");

        pk    ("id",                  Type.INTEGER,          "Идентификатор поля");
        
        fk    ("object_model_id",     VocRdList.class,       "Идентификатор модели");
        fk    ("property_value_type", VocRdColType.class,    "Тип");
        
        col   ("link_dictionary",     Type.INTEGER, null,    "Ссылка на справочник (для property_value_type = 6)");
        
        col   ("name",                Type.STRING,           "Наименование");
        
        col   (RD_NSI_REF_COL_NAME,  Type.STRING, 20, null, "Код соответствующего элемента справочника НСИ " + PASSPORT_FIELDS_LIST_NSI_REGISTRY_NUMBER);

        key   ("object_model_id", "object_model_id");
        
    }
        
}