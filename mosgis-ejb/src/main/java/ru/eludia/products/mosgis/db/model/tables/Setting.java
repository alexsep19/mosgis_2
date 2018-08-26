package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;

public class Setting extends View {

    public Setting () {
        
        super  ("tb_settings", "Настройки системы");
        
        pk    ("id",    Type.STRING,       "Ключ");        
        col   ("label", Type.STRING,       "Наименование");
        col   ("value", Type.STRING,       "Значение");
        
    }

    @Override
    public final String getSQL () {

        return "SELECT " +
            " v.id" +
            " , v.label" +
            " , NVL (t.value, v.value) value" +
            " FROM "      + getName (VocSetting.class)   + " v" +
            " LEFT JOIN " + getName (SettingValue.class) + " t ON v.id = t.id_vc_setting";

    }

}