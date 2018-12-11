package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;

public class PublicPropertyContractVotingProtocol extends EnTable {
    
    public enum c implements EnColEnum {
        
        UUID_CTR       (PublicPropertyContract.class,  "Договор на пользование общим имуществом"),
        UUID_VP        (VotingProtocol.class,          "Протокол ОСС"),
        ;
        
        @Override
        public boolean isLoggable () {
            switch (this) {
//                case ID_LOG:
//                    return false;
                default:
                    return true;
            }
        }

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
    }
    
    public PublicPropertyContractVotingProtocol () {        
        super ("tb_pp_ctr_vp", "Связь между ДПОИ и ОСС");
        cols (c.class);
        key ("uuid_ctr", c.UUID_CTR);        
    }    
    
}
