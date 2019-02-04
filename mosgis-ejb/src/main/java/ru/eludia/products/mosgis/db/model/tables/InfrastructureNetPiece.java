package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.NUMERIC;
import static ru.eludia.base.model.Type.STRING;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;

public class InfrastructureNetPiece  extends EnTable {
    
    public enum c implements EnColEnum {
        
        UUID_OKI       (Infrastructure.class, "Ссылка на объект ОКИ"),
        
        NAME           (STRING, 140, null, "Наименование участка"),
        
        DIAMETER       (NUMERIC, 9, 3, "Диаметр (мм)"),
        LENGTH         (NUMERIC, 9, 3, "Протяженность (км)"),
        NEEDREPLACED   (NUMERIC, 9, 3, null ,"Нуждется в замене (км)"),
        
        WEAROUT        (NUMERIC, 3, 1, null, "Износ (%)"),
        
        CODE_VC_NSI_36 (STRING, 20, null, "Уровень давления газопровода (НСИ 36)"),
        CODE_VC_NSI_45 (STRING, 20, null, "Уровень напряжения (НСИ 45)"),
        
        ID_LOG         (InfrastructureNetPieceLog.class, "Посленее событие редактирования")
        
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
            switch (this) {
                case UUID_OKI:
                case ID_LOG:
                    return false;
                default:
                    return true;
            }
        }
        
    }
    
    public InfrastructureNetPiece () {
        
        super ("tb_oki_net_pieces", "Сведения об участках сети ОКИ");
        
        cols (c.class);
        
        trigger ("BEFORE INSERT OR UPDATE", ""
                + "BEGIN "
                + "IF NVL (:NEW.needreplaced, 0) > :NEW.length THEN "
                    + "raise_application_error (-20000, 'Общая длина участка не должна быть меньше, чем длина, нуждающася в замене'); "
                + "END IF; "
                + "END;"
        );
        
    }
    
}
