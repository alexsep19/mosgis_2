package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Bool.TRUE;

public class VocNsiPassportFieldsSrcTable extends Table {

    /**
     * Это описание таблицы нужно, чтобы VocPassportFields
     * могла быть успешно создана до загрузки НСИ.
     * 
     * Если из сервиса ГИС ЖКХ поступит что-то другое, 
     * по крайней мере, указанные поля будут присутствовать.
     */
    public VocNsiPassportFieldsSrcTable () {

        super  (VocPassportFields.getSrcTableName (),         "Форма описания объектов ЖФ");

        pk     ("guid",               Type.UUID,               "Глобально-уникальный идентификатор элемента справочника");        
        col    ("parent",             Type.UUID, null,         "Ссылка на родительскую запись");        
        col    ("code",               Type.STRING, 20,         "Код элемента справочника, уникальный в пределах справочника");
        col    ("isactual",           Type.BOOLEAN, TRUE,      "Признак актуальности элемента справочника");        
        
        col    ("f_b2d4654420",       Type.UUID, null,         "Использование для дифференциации");        

        col    ("f_d121e7e83c",       Type.BOOLEAN, null,      "Наличие значения");
        col    ("f_5d4e6bfa13",       Type.BOOLEAN, null,      "Ведение бизнес-истории");
        col    ("f_2676d2ef8f",       Type.BOOLEAN, null,      "Общее имущество");
        col    ("f_ee615eea5b",       Type.BOOLEAN, null,      "Обязательность");

        col    ("f_f14793f679",       Type.INTEGER, null,      "Справочник  объектов ЖФ");
        col    ("f_82f23dfb08",       Type.STRING, null,       "Тип значения описания объектов ЖФ");
        col    ("f_dec8f6b617",       Type.STRING, null,       "Наследовать с");
        col    ("f_dfde1ffd13",       Type.STRING, null,       "Наименование");
        col    ("f_d2fc7cb771",       Type.STRING, null,       "Сортировка");
        col    ("f_c6e5a29665",       Type.STRING, null,       "Единица измерения");
        col    ("f_ba53e8bbe6",       Type.STRING, null,       "Раздел");
        col    ("f_38ca0af80c",       Type.STRING, null,       "Описание");
        col    ("f_d27ad7aa04",       Type.STRING, null,       "Применимость к объекту");

        col    ("f_1042604587",       Type.DATE, null,         "Дата начала действия");
        col    ("f_d6d10f61bc",       Type.DATE, null,         "Дата окончания действия");

    }

}