package ru.eludia.products.mosgis.db.model.voc;

import javax.json.Json;
import ru.eludia.base.model.Type;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Table;

public class VocMeteringDeviceType extends Table {
    
    private static final String TABLE_NAME = "vc_meter_types";
    
    public VocMeteringDeviceType () {       
        super (TABLE_NAME, "Типы приборов учёта");
        cols  (c.class);        
        pk    (c.ID);
        data  (i.class);
    }
    
    public enum c implements ColEnum {        
        ID         (Type.NUMERIC, 1, "Идентификатор"),
        LABEL      (Type.STRING,     "Наименование");        
                                                                                    @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);}
    }
    
    public enum i {
     
/*
CollectiveDevice 	Характеристики общедомового ПУ (тип прибора учета = "Коллективный (общедомовой)")

NonResidentialPremiseDevice 	Характеристики ИПУ нежилого помещения (тип прибора учета = "Индивидуальный")
ResidentialPremiseDevice 	Характеристики ИПУ жилого помещения (тип прибора учета = "Индивидуальный")
LivingRoomDevice 	        Характеристики комнатного ИПУ (тип прибора учета = "Комнатный")
CollectiveApartmentDevice 	Характеристики общеквартирного ПУ (для квартир коммунального заселения) (тип прибора учета = "Общий (квартирный)")

ApartmentHouseDevice 	Характеристики ИПУ жилого дома (тип прибора учета = "Индивидуальный", тип дома = "Жилой")
*/        
        APARTMENT_HOUSE         (1, "ИПУ жилого дома"),
        COLLECTIVE              (2, "общедомовой ПУ"),       
        NON_RESIDENTIAL_PREMISE (3, "ИПУ нежилого помещения"),
        RESIDENTIAL_PREMISE     (4, "ИПУ жилого помещения"),
        COLLECTIVE_APARTMENT    (5, "общеквартирный ПУ"),
        LIVING_ROOM             (6, "комнатный ИПУ"),
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

    }
    
    private static JsonArray jsonArray = i.toJsonArray ();    
    public  static final void addTo (JsonObjectBuilder job) {
        job.add (TABLE_NAME, jsonArray);
    }
    
}