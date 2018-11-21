package ru.eludia.products.mosgis.db.model;

import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;

public abstract class GisWsLogTable extends LogTable {

    public GisWsLogTable (String name, String remark, Class object, Class... colEnums) {
        
        super (name, remark, object, colEnums);
        
        fk    ("uuid_out_soap",             OutSoap.class,                      null,   "Последний запрос на импорт в ГИС ЖКХ");
        col   ("uuid_message",              Type.UUID,                          null,   "UUID запроса в ГИС ЖКХ");
                
    }

}