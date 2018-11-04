package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class PropertyDocumentLog extends LogTable {

    public PropertyDocumentLog () {

        super ("tb_prop_docs__log", "История редактирования документов о правах собственности", PropertyDocument.class
            , EnTable.c.class
            , PropertyDocument.c.class
        );
        
    }
                
}