package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;

public class SettingValue extends Table {

    public SettingValue () {
        
        super ("tb_setting_values", "Значения параметров настройки системы");
        
        pk    ("id_vc_setting",  Type.STRING, "Параметр");
        
        col   ("value",        Type.STRING, null,            "Значение");
        
    }
    
}