package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.LogTable;

public class PublicPropertyContractVotingProtocolLog extends LogTable {
    
    public PublicPropertyContractVotingProtocolLog () {
    
        super ("tb_pp_ctr_vp__log", "История редактирования связи между ДПОИ и ОСС", PublicPropertyContractVotingProtocol.class,
            EnTable.c.class,
            PublicPropertyContractVotingProtocol.c.class
        );
    
    }
    
}