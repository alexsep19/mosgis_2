package ru.eludia.products.mosgis.db.model;

import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.ws.WsMessages;

public abstract class GisWsLogTable extends LogTable {

    public GisWsLogTable (String name, String remark, Class object, Class... colEnums) {
        
        super (name, remark, object, colEnums);
        
        fk    ("uuid_out_soap",             OutSoap.class,                      null,   "Запрос на импорт в ГИС ЖКХ");
        fk    ("uuid_in_soap",              WsMessages.class,                   null,   "Запрос, принятый от поставщика данных");
        col   ("uuid_message",              Type.UUID,                          null,   "UUID запроса в ГИС ЖКХ");
                
    }

}