package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class OverhaulShortProgramDocumentLog extends LogTable {
    
    public OverhaulShortProgramDocumentLog () {
        
        super ("tb_oh_shrt_pr_docs__log", "Документы краткосрочного плана капитального ремонта: история изменений",
                OverhaulShortProgramDocument.class,
                EnTable.c.class,
                OverhaulShortProgramDocument.c.class
        );
        
    }
    
}