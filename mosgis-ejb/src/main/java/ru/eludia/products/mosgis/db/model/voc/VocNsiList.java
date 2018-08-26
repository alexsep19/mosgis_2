package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;

public class VocNsiList extends Table {

    public VocNsiList () {
        
        super ("vc_nsi_list", "Перечень справочников НСИ ГИС ЖКХ");

        pk    ("registrynumber", Type.INTEGER,          "Реестровый номер справочника");        
        col   ("name",           Type.STRING,           "Наименование справочника");
        fk    ("listgroup",      VocNsiListGroup.class, "Группа");
        col   ("cols",           Type.TEXT, null,       "Список столбцов в виде JSON");
        col   ("ts_last_import", Type.DATETIME, null,   "Дата последнего импорта");
        fk    ("uuid_out_soap",  OutSoap.class, null,   "Последний запрос на импорт из ГИС ЖКХ");

        key   ("name", "name");
        
    }
        
}