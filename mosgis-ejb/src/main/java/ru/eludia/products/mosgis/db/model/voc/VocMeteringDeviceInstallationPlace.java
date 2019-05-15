package ru.eludia.products.mosgis.db.model.voc;

import javax.json.Json;
import ru.eludia.base.model.Type;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.jms.xl.base.XLException;

public class VocMeteringDeviceInstallationPlace extends Table {
    
    private static final String TABLE_NAME = "vc_meter_places";
    
    public VocMeteringDeviceInstallationPlace () {       
        super (TABLE_NAME, "Типы приборов учёта");
        cols  (c.class);        
        pk    (c.ID);
        data  (i.class);
    }
    
    public enum c implements ColEnum {        
        
        ID             (Type.STRING,                "Идентификатор"),
        LABEL_POWER    (Type.STRING,                "Наименование (электричество)"),
        LABEL_OTHER    (Type.STRING,                "Наименование (не электричество)"),
        ;
        
        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}        
        
    }
    
    public enum i {
     
        IN  ("in", "На входе", "На подающем трубопроводе"),
        OUT ("out", "На выходе", "На обратном трубопроводе"),
        ;
                
        String id;
        String label_power;
        String label_other;

        public String getId () {
            return id;
        }

        public String getLabel_other () {
            return label_other;
        }

        public String getLabel_power () {
            return label_power;
        }
        
        private i (String id, String label_power, String label_other) {
            this.id = id;
            this.label_power = label_power;
            this.label_other = label_other;
        }

        private JsonObject toPowerJsonObject () {
            return Json.createObjectBuilder ()
                .add ("id",    id)
                .add ("label", label_power)
                .build ();
        }
        
        private JsonObject toOtherJsonObject () {
            return Json.createObjectBuilder ()
                .add ("id",    id)
                .add ("label", label_other)
                .build ();
        }

        public static JsonArray toPowerJsonArray () {            
            JsonArrayBuilder builder = Json.createArrayBuilder ();                    
            for (i value: i.values ()) builder.add (value.toPowerJsonObject ());            
            return builder.build ();            
        }
        
        public static JsonArray toOtherJsonArray () {            
            JsonArrayBuilder builder = Json.createArrayBuilder ();                    
            for (i value: i.values ()) builder.add (value.toOtherJsonObject ());            
            return builder.build ();            
        }
        
        public static i fromXL (String s) throws XLException {
            switch (s) {
                case "На входе/на подающем трубопроводе": return IN;
                case "На выходе/на обратном трубопроводе": return OUT;
                default: throw new XLException (s +" пока поддерживается");
            }
        }
    }
    
    private static JsonArray powerJsonArray = i.toPowerJsonArray ();
    private static JsonArray otherJsonArray = i.toPowerJsonArray ();
    
    public  static final void addTo (JsonObjectBuilder job, boolean isPower) {
        job.add (TABLE_NAME, isPower ? powerJsonArray : otherJsonArray);
    }
    
}