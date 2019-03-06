package ru.eludia.products.mosgis.db.model.voc.nsi;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;

public class Nsi237 extends View {
    
    public enum c implements ColEnum {
        
        ID     (Type.STRING, 20, null, "Код"),
        LABEL  (Type.STRING,     null, "Наименование"),
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
                
    }    

    public Nsi237 () {        
        super  ("vw_nsi_237", "Коды субъектов Российской Федерации (регионов)");
        cols   (c.class);
        pk     (c.ID);        
    }

    @Override
    public final String getSQL () {

        return "SELECT "
            + " code id, "
            + VocNsi237.c.F_64B1C12EF9.lc () + " || ' ' || " + VocNsi237.c.f_356213bef6.lc () + " label "
            + "FROM "
            + " vc_nsi_237 "
            + "WHERE"
            + " isactual=1"
        ;

    }
        
}