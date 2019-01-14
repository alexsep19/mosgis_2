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
        
    }

}