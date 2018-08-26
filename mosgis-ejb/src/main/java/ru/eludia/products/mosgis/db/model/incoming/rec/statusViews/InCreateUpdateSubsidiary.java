package ru.eludia.products.mosgis.db.model.incoming.rec.statusViews;

import ru.eludia.products.mosgis.db.model.incoming.json.InImportSubsidiaries;
import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.incoming.rec.InCreateSubsidiary;
import ru.eludia.products.mosgis.db.model.incoming.rec.InUpdateSubsidiary;
import ru.eludia.products.mosgis.db.model.incoming.soap.InSoap;

public class InCreateUpdateSubsidiary extends View {

    public InCreateUpdateSubsidiary () {
        
        super  ("in_tb_crup_subsidiaries",                     "Даты обработки входящих запросов на регистрацию/изменение отдельных новых юрлиц");
        
        pk     ("uuid",           Type.UUID,                   "Ключ (TransportGUID)");
        col    ("is_done",        Type.BOOLEAN,    null,       "Дата/время обработки");
        ref    ("uuid_in_soap",   InSoap.class,                "Ссылка на SOAP-пакет");
        
    }

    private void append (StringBuilder sb, Class table) {        

        sb.append ("SELECT ");

        sb.append ( "js.uuid,");
        sb.append ( "js.uuid_in_soap,");
        sb.append ( "DECODE(tb.ts_done, NULL, 0, 1) is_done ");
        sb.append ("FROM ");
        
        sb.append ( getName (InImportSubsidiaries.class));
        sb.append ( " js ");
        
        sb.append ( "INNER JOIN ");
        sb.append ( getName (table));
        sb.append ( " tb");
        sb.append ( " ON tb.uuid_in_import = js.uuid");
        
    }

    @Override
    public final String getSQL () {
        
        StringBuilder sb = new StringBuilder ();
        
        append (sb, InCreateSubsidiary.class);
        
        sb.append (" UNION ");
        
        append (sb, InUpdateSubsidiary.class);
        
        return sb.toString ();
    
    }

}