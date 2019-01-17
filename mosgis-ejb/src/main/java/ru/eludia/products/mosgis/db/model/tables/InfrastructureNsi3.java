package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.STRING;
import ru.eludia.products.mosgis.db.model.EnColEnum;

public class InfrastructureNsi3 extends Table {
    
    public enum c implements EnColEnum {
        
        UUID (Infrastructure.class, "Объект коммунальной инфраструктуры"),
        CODE (STRING, 20,           "Ссылка на НСИ \"Вид коммунальной услуги\" (НСИ 3)")
        
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
            return false;
        }
        
    }
    
    public InfrastructureNsi3 () {
        
        super ("tb_infrastructures_nsi_3", "Виды коммунальных услуг коммунальных инфраструктур");
        
        cols (c.class);
        
        pk (c.UUID);
        pk (c.CODE);
        
    }
    
}