package ru.eludia.products.mosgis.db.model.voc.nsi;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;

public class Nsi16 extends View {
    
    public enum c implements ColEnum {
        
        ID                        (Type.STRING,   null,           "Код"),
        LABEL                     (Type.STRING,   null,           "Наименование"),
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
            + " code id, "
            + " f_17e03ccf84 || ' ' || f_a008c7d1f3 label "
            + "FROM "
            + " vc_nsi_16 "
            + "WHERE"
            + " isactual=1"
        ;

    }
        
}