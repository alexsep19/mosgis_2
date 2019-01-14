package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnTable;

public class VocUnom extends EnTable {

    public enum c implements ColEnum {
        
        UNOM          (Type.NUMERIC, 15,     "UNOM"),
        KLADR         (Type.STRING,  null,   "КЛАДР"),
        FIAS          (Type.STRING,  null,   "Исходный код ФИАС"),
        KAD_N         (Type.STRING,  null,   "Кадастровый номер"),
        FIASHOUSEGUID (VocBuilding.class,    "Код ФИАС, выверенный по справочнику"),
        ID_STATUS     (VocUnomStatus.class,  "Статус"),
        ;

        @Override
        public Col getCol() {
            return col;
        }
        private Col col;

        private c(Type type, Object... p) {
            col = new Col(this, type, p);
        }

        private c(Class c, Object... p) {
            col = new Ref(this, c, p);
        }

    }

    public VocUnom () {

        super ("vc_unom", "Мапинг UNOM");

        cols (c.class);

        key ("unom", "unom");
        key ("fiashouseguid", "fiashouseguid");
        
        trigger ("BEFORE INSERT OR UPDATE", 
                
            "BEGIN "
                    
                + "IF :NEW.ID_STATUS = " + VocUnomStatus.i.DUPLICATED_FIAS + " THEN RETURN; END IF; "
                    
                + "IF :NEW.FIAS IS NULL THEN :NEW.ID_STATUS := " + VocUnomStatus.i.EMPTY_FIAS + "; ELSE BEGIN "
                        
                    + "IF NOT REGEXP_LIKE (UPPER(:NEW.FIAS), '^([A-F0-9]{8})-([A-F0-9]{4})-([A-F0-9]{4})-([A-F0-9]{4})-([A-F0-9]{12})$') THEN :NEW.ID_STATUS := " + VocUnomStatus.i.INVALID_FIAS + "; ELSE BEGIN "                            
                        + "SELECT houseguid INTO :NEW.FIASHOUSEGUID FROM vc_buildings WHERE houseguid = HEXTORAW(REPLACE(UPPER(:NEW.FIAS), '-', ''));"
                        + ":NEW.ID_STATUS := " + VocUnomStatus.i.OK + "; "        
                        + "EXCEPTION WHEN NO_DATA_FOUND THEN :NEW.ID_STATUS := " + VocUnomStatus.i.UNKNOWN_FIAS + "; "                            
                    + "END; END IF;"
                        
                + "END; END IF;"
                    
            + "END;"
                
        );
        
        
    }

}