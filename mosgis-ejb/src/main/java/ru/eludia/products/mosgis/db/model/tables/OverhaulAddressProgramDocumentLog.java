package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class OverhaulAddressProgramDocumentLog extends LogTable {
    
    public OverhaulAddressProgramDocumentLog () {
        
        super ("tb_oh_addr_pr_docs__log", "Документы адресного плана капитального ремонта: история изменений",
                OverhaulAddressProgramDocument.class,
                EnTable.c.class,
                OverhaulAddressProgramDocument.c.class
        );
        
    }
    
}