package ru.eludia.products.mosgis.db.model.voc.nsi;

import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.ModelHolder;

public class Nsi331 extends View {
    
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

    public Nsi331 () {        
        super  ("vw_nsi_331", "Составляющая стоимости электрической энергии");
        cols   (c.class);
        pk     (c.ID);        
    }

    @Override
    public final String getSQL () {

        return "SELECT "
            + " code id, "
            + VocNsi331.c.F_B4AE364F08.name () + " label "
            + "FROM "
            + " vc_nsi_331 "
            + "WHERE"
            + " isactual=1"
        ;

    }
    
    public static Select getVocSelect () {

        return ModelHolder.getModel ()
            .select (Nsi331.class, "AS vc_nsi_331", "*")
            .orderBy (Nsi331.c.ID);
        
    }

}