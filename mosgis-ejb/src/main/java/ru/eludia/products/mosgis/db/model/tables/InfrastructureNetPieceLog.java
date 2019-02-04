package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.LogTable;
import ru.eludia.products.mosgis.db.model.EnTable;

public class InfrastructureNetPieceLog extends LogTable {
    
    public InfrastructureNetPieceLog () {
    
        super ("tb_oki_net_pieces__log", "История измененеий участков сети ОКИ", 
                InfrastructureNetPiece.class, 
                EnTable.c.class , 
                InfrastructureNetPiece.c.class
        );
        
    }
    
}
