package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisFileLogTable;

public class PlannedExaminationFileLog extends GisFileLogTable {

    public PlannedExaminationFileLog () {

        super ("tb_pln_exm_files__log", "История редактирования файлов плановых проверок", PlannedExaminationFile.class
            , EnTable.c.class
            , AttachTable.c.class
            , PlannedExaminationFile.c.class
        );
        
    }
    
}