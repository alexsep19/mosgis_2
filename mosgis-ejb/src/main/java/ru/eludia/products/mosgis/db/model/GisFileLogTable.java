package ru.eludia.products.mosgis.db.model;

import ru.eludia.base.model.Type;

public abstract class GisFileLogTable extends LogTable {

    public GisFileLogTable (String name, String remark, Class object, Class... colEnums) {
        
        super (name, remark, object, colEnums);
        
        col    ("ts_start_sending",      Type.DATE,                null,        "Дата начала передачи");
        col    ("ts_error",              Type.TIMESTAMP,           null,        "Дата/время ошибки передачи");
        col    ("err_text",              Type.STRING,              null,        "Текст ошибки");
                
    }

}