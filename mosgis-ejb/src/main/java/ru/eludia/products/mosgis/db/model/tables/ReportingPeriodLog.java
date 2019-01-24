package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;

public class ReportingPeriodLog extends GisWsLogTable {

    public ReportingPeriodLog () {

        super ("tb_reporting_periods__log", "История редактирования периодов отчётности в планах работ и услуг", ReportingPeriod.class
            , EnTable.c.class
            , ReportingPeriod.c.class
        );
        
    }
    
}