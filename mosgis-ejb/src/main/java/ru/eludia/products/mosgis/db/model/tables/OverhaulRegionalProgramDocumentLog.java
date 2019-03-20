package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class OverhaulRegionalProgramDocumentLog extends LogTable {
    
    public OverhaulRegionalProgramDocumentLog () {
        
        super ("tb_oh_reg_pr_docs__log", "Документы регионального плана капитального ремонта: история изменений",
                OverhaulRegionalProgramDocument.class,
                EnTable.c.class,
                OverhaulRegionalProgramDocument.c.class
        );
        
    }
    
}
