package ru.eludia.products.mosgis.db.model.incoming.nsi;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.View;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState;

public class InNsiItemStatusView extends View {

    public InNsiItemStatusView () {
        
        super  ("in_nsi_items_status_view",     "Статусы импорта справочников НСИ");

        pk    ("uuid",              Type.UUID,              "Ключ");
        ref   ("uuid_in_nsi_group", InNsiGroup.class, null, "Импорт группы");
        col   ("ts_group",          Type.TIMESTAMP,   null, "Дата/время запуска импорта группы");
        col   ("ts",                Type.TIMESTAMP,   null, "Дата/время запуска импорта справочника");
        col   ("ts_rq",             Type.TIMESTAMP,   null, "Дата/время SOAP-запроса importNsiItem");
        col   ("ts_rp",             Type.TIMESTAMP,   null, "Дата/время SOAP-ответа getState");
        col   ("registrynumber",    Type.INTEGER,     null, "Реестровый номер справочника");
        col   ("page",              Type.INTEGER,     null, "Номер страницы");        
        col   ("is_failed",         Type.BOOLEAN,     null, "1 для аварийных, 0 для остальных");
        col   ("err_code",          Type.STRING,      null, "Код ошибки");
        col   ("err_text",          Type.STRING,      null, "Текст ошибки");
        fk    ("id_status",         VocAsyncRequestState.class, null, "Статус");        
        
    }
    
    @Override
    public final String getSQL () {
                
        return "SELECT  " +
            "  i.uuid " +
            "  , g.ts ts_group " +
            "  , i.ts          " +
            "  , o.ts ts_rq    " +
            "  , o.ts_rp       " +
            "  , i.uuid_in_nsi_group " +
            "  , i.registrynumber " +
            "  , i.page       " +
            "  , o.id_status  " +
            "  , o.err_code   " +
            "  , o.is_failed  " +
            "  , o.err_text   " +
            "FROM  "       + getName (InNsiItem.class)  + " i " +
            " INNER JOIN " + getName (InNsiGroup.class) + " g ON i.uuid_in_nsi_group = g.uuid" +
            " LEFT  JOIN " + getName (OutSoap.class)    + " o ON i.uuid              = o.uuid";

    }
    
}
