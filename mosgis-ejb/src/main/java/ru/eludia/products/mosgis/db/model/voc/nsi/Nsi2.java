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

public class Nsi2 extends View {
    
    public enum c implements ColEnum {
        
        CODE   (Type.STRING, 20, null, "Код"),
        ID     (Type.NUMERIC,    null, "Битовая маска"),
        LABEL  (Type.STRING,     null, "Наименование"),
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
                
    }    

    public Nsi2 () {        
        super  ("vw_nsi_2", "Вид коммунального ресурса");
        cols   (c.class);
        pk     (c.ID);        
    }

    @Override
    public final String getSQL () {

        return "SELECT "
            + " code, "
            + " POWER (2, code - 1) id, "
            + VocNsi2.c.F_C8D77DDAD5.name () + " label "
            + "FROM "
            + " vc_nsi_2 "
            + "WHERE"
            + " isactual=1"
        ;

    }
    
    public enum i {
        
        COLD_WATER           (1, 1,   "Холодная вода"),
        HOT_WATER            (2, 2,   "Горячая вода"),
        POWER                (3, 4,   "Электрическая энергия"),
        GAS                  (4, 8,   "Газ"),
        HEAT                 (5, 16,  "Тепловая энергия"),

        HEAT_HOT_WATER       (0, HEAT.getId () | HOT_WATER.getId (),  "Тепловая энергия + горячая вода"),
        HEAT_COLD_WATER      (0, HEAT.getId () | COLD_WATER.getId (),  "Тепловая энергия + холодная вода"),
        HEAT_WATER           (0, HEAT.getId () | HOT_WATER.getId () | COLD_WATER.getId (),  "Тепловая энергия + холодная и горячая вода"),
        
        WASTE_WATER          (8, 128, "Сточные воды"),

        ;
                
        int    id;
        int    code;
        String label;

        public int getCode () {
            return code;
        }

        public int getId () {
            return id;
        }
        
        public String getLabel () {
            return label;
        }

        private i (int code, int id, String label) {
            this.code  = code;
            this.id    = id;
            this.label = label;            
        }
        
        private JsonObject toJsonObject () {
            return Json.createObjectBuilder ()
                .add ("id",    id)
                .add ("label", label)
            .build ();
        }

        public static JsonArray toMeteringJsonArray () {            
            JsonArrayBuilder builder = Json.createArrayBuilder ();                    
                builder.add (COLD_WATER.toJsonObject ());            
                builder.add (HOT_WATER.toJsonObject ());            
                builder.add (POWER.toJsonObject ());            
                builder.add (GAS.toJsonObject ());            
                builder.add (HEAT.toJsonObject ());            
                builder.add (HEAT_HOT_WATER.toJsonObject ());            
                builder.add (HEAT_COLD_WATER.toJsonObject ());            
                builder.add (HEAT_WATER.toJsonObject ());
                builder.add (WASTE_WATER.toJsonObject ());            
            return builder.build ();            
        }
        
        private static JsonArray meteringJsonArray = i.toMeteringJsonArray ();
        
        public  static final void addMeteringTo (JsonObjectBuilder job) {
            job.add ("vc_nsi_2", meteringJsonArray);
        }        
                
    }        

}