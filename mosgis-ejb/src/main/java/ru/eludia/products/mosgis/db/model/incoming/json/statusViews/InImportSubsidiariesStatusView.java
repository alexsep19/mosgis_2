package ru.eludia.products.mosgis.db.model.incoming.json.statusViews;

import ru.eludia.base.model.View;
import ru.eludia.products.mosgis.db.model.incoming.rec.statusViews.InCreateUpdateSubsidiary;
import ru.eludia.products.mosgis.db.model.incoming.soap.InSoap;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncRequestState;

public class InImportSubsidiariesStatusView extends View {

    public InImportSubsidiariesStatusView () {
        
        super  ("in_js_import_subsidiaries_st",     "Статусы отработки запросов на регистрацию/изменение отдельных новых юрлиц");

        ref    ("uuid",           InCreateUpdateSubsidiary.class, "Ключ (TransportGUID)");
        ref    ("uuid_in_soap",   InSoap.class,                   "SOAP-пакет");
        fk     ("id_status",      VocAsyncRequestState.class,     "Статус");
        
    }
    
    @Override
    public final String getSQL () {
        
        String cols = "uuid, uuid_in_soap";
        
        return "SELECT " + cols
        + ", CASE "
        + " WHEN SUM (is_done) < COUNT (*) THEN " + VocAsyncRequestState.i.IN_PROGRESS.getId ()
        +                                " ELSE " + VocAsyncRequestState.i.DONE.getId ()
        + " END id_status"
        + " FROM "     + getName (InCreateUpdateSubsidiary.class)
        + " GROUP BY " + cols
        ;

    }
    
}