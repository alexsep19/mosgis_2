package ru.eludia.products.mosgis.db.model.incoming.nsi;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.View;

public class InNsiItemStatsOverview extends View {

    public InNsiItemStatsOverview () {
        
        super  ("in_nsi_items_stats_overview",     "Статистика импорта справочников НСИ");

        col   ("started",           Type.TIMESTAMP,   null, "Дата/время запуска импорта");
        col   ("done",              Type.INTEGER,     null, "Количество завершённых импортов");
        col   ("pending",           Type.INTEGER,     null, "Количество импортов в очереди");
        
    }
    
    @Override
    public final String getSQL () {
                
        return "WITH src AS (" +
            "  SELECT " +
            "    uuid_in_nsi_group " +
            "    , ts_group " +
            "    , DECODE (ts_rp, NULL, 0, 1) done " +
            "    , DECODE (ts_rp, NULL, 1, 0) pending " +
            "  FROM " +
            "    " + getName (InNsiItemStatusView.class)    + " " +
            "  WHERE " +
            "    uuid IN ( " +
            "      SELECT uuid FROM in_nsi_items " +
            "      MINUS " +
            "      SELECT uuid FROM out_soap " +
            "    ) " +
            ") " +
            ", stat AS ( " +
            "  SELECT " +
            "    uuid_in_nsi_group " +
            "    , ts_group started " +
            "    , SUM (done) done " +
            "    , SUM (pending) pending " +
            "  FROM " +
            "    src " +
            "  GROUP BY " +
            "    uuid_in_nsi_group " +
            "    , ts_group " +
            ") " +
            "SELECT " +
            "  MIN (started) started " +
            "  , SUM (done) done " +
            "  , SUM (pending) pending " +
            "FROM " +
            "  stat";

    }
    
}
