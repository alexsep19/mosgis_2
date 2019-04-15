package ru.eludia.products.mosgis.db.model.voc.nsi;

import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.ModelHolder;

public class Nsi275 extends View {
    
    public enum c implements ColEnum {
        
        CODE   (Type.STRING, 20, null, "Код"),
        ID     (Type.NUMERIC,    null, "Битовая маска"),
        LABEL  (Type.STRING,     null, "Наименование"),
        GUID   (Type.UUID,       null, "Глобально-уникальный идентификатор элемента справочника")
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
                
    }    

    public Nsi275 () {        
        super  ("vw_nsi_275", "Ограничение объема предоставляемой компенсации расходов");
        cols   (c.class);
        pk     (c.ID);        
    }

    @Override
    public final String getSQL () {

        return "SELECT "
            + " v.code "
            + ", v." + VocNsi275.c.F_FDD6069376.name () + " label "
            + ", v.guid "
            + " FROM "
            + " vc_nsi_275 v "
            + " WHERE"
            + " isactual=1"
        ;

    }

    public static Select getVocSelect () {
        
        return ModelHolder.getModel ().select (Nsi275.class, "AS vc_nsi_275"
            , c.CODE.lc () + " AS id"
            , c.LABEL.lc ()
        ).orderBy (c.LABEL);
        
    }    

}