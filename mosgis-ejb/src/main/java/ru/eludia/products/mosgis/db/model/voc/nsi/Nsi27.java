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

public class Nsi27 extends View {
    
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

    public Nsi27 () {        
        super  ("vw_nsi_27", "Типы приборов учёта");
        cols   (c.class);
        pk     (c.ID);        
    }

    @Override
    public final String getSQL () {

        return "SELECT "
            + " code id, "
            + " f_bcab5caf38 label "
            + "FROM "
            + " vc_nsi_27 "
            + "WHERE"
            + " isactual=1"
        ;

    }
    
    public enum i {
        
        INDIVIDUAL           (1, "Индивидуальный"),
        COLLECTIVE           (2, "Коллективный (общедомовой)"),
        COLLECTIVE_APARTMENT (3, "Общий (квартирный)"),
        LIVING_ROOM          (4, "Комнатный"),
        ;
                
        int    id;
        String label;

        public int getId () {
            return id;
        }
        
        public String getLabel () {
            return label;
        }

        private i (int id, String label) {
            this.id = id;
            this.label = label;            
        }
        
        private JsonObject toJsonObject () {
            return Json.createObjectBuilder ()
                .add ("id",    id)
                .add ("label", label)
            .build ();
        }

        public static JsonArray toJsonArray () {            
            JsonArrayBuilder builder = Json.createArrayBuilder ();                    
            for (i value: i.values ()) builder.add (value.toJsonObject ());            
            return builder.build ();            
        }
        
        private static JsonArray jsonArray = i.toJsonArray ();
        
        public  static final void addTo (JsonObjectBuilder job) {
            job.add ("vc_nsi_27", jsonArray);
        }        
                
    }        
    
}