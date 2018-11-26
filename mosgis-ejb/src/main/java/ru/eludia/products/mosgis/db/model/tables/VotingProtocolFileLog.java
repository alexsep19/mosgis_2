package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisFileLogTable;

public class VotingProtocolFileLog extends GisFileLogTable {
    
    public VotingProtocolFileLog () {
        
        super ("tb_voting_protocol_files__log", "История редактирования файлов, приложенных к протоколу ОСС", VotingProtocolFile.class
            , EnTable.c.class
            , AttachTable.c.class
            , VotingProtocolFile.c.class
        );
    }
}
