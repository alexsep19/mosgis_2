package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class VoteDecisionListLog extends LogTable {
    
    public VoteDecisionListLog () {
    
        super ("tb_vote_decision_lists__log", "История редактирования повесток ОСС", VoteDecisionList.class,
            EnTable.c.class,
            VoteDecisionList.c.class
        );
    
    }
    
}