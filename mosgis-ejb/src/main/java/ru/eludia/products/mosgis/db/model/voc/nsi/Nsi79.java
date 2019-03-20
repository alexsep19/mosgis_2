package ru.eludia.products.mosgis.db.model.voc.nsi;

import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.View;
import ru.eludia.products.mosgis.db.ModelHolder;

public class Nsi79 extends View {
    
    public enum c implements ColEnum {
        
        ID    (Type.STRING, 20, null, "Код"),
        GUID  (Type.UUID,             "Глобально-уникальный идентификатор элемента справочника"),
        LABEL (Type.STRING,     null, "Наименование"),
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
                
    }    

    public Nsi79 () {        
        super  ("vw_nsi_79", "Виды документов программы");
        cols   (c.class);
        pk     (c.ID);
    }

    @Override
    public final String getSQL () {

        return "SELECT "
            + " 0+code id, "
            + " " + VocNsi79.c.F_BCE3C198BF.name () + " label, "
            + " " + VocNsi79.c.GUID.name () + " guid "
            + "FROM "
            + " vc_nsi_79 "
            + "WHERE"
            + " isactual=1"
        ;

    }

    public static Select getVocSelect () {

        return ModelHolder.getModel ()
            .select (Nsi79.class, "AS vc_nsi_79", "*")
            .orderBy (c.ID);
        
    }
    
}
