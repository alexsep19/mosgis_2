package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;

public class VocRdList extends Table {

    public VocRdList () {

        super ("vc_rd_list", "Перечень справочников ГИС РД");

        col   ("parent",         Type.INTEGER, null,    "Ссылка на охватывающую модель");
        pk    ("modelid",        Type.INTEGER,          "Идентификатор модели");        
        col   ("name",           Type.STRING,           "Наименование");
//        col   ("ts_last_import", Type.DATETIME, null,   "Дата последнего импорта");
//        fk    ("uuid_out_soap",  OutSoap.class, null,   "Последний запрос на импорт из ГИС РД");

        key   ("name", "name");

        item (
            "modelid", 1,
            "name", "МКД"
        );

    }

}