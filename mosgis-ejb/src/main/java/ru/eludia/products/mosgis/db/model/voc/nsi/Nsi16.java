package ru.eludia.products.mosgis.db.model.voc.nsi;

import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.ejb.ModelHolder;

public class Nsi16 extends View {
    
    public enum c implements ColEnum {
        
        ID    (Type.STRING, 20, null, "Код"),
        LABEL (Type.STRING,     null, "Наименование"),
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
                
    }    

    public Nsi16 () {        
        super  ("vw_nsi_16", "Помещения и блоки");
        cols   (c.class);
        pk     (c.ID);        
    }

    @Override
    public final String getSQL () {

        return "SELECT "
            + " 0+code id, "
            + " " + VocNsi16.c.F_17E03CCF84.name () + " || ' ' || " + VocNsi16.c.F_A008C7D1F3.name () + " label "
            + "FROM "
            + " vc_nsi_16 "
            + "WHERE"
            + " isactual=1"
        ;

    }

    public static Select getVocSelect () {

        return ModelHolder.getModel ()
            .select (Nsi16.class, "AS vc_nsi_16", "*")
            .orderBy (c.ID);
        
    }

}