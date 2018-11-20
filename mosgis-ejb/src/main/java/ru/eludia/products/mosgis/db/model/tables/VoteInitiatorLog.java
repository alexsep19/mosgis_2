package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class VoteInitiatorLog extends LogTable {
    
    public VoteInitiatorLog () {
    
        super ("tb_vote_initiators__log", "История редактирования инициаторов собрания", VoteInitiator.class,
            EnTable.c.class,
            VoteInitiator.c.class
        );
    
    }
    
}
