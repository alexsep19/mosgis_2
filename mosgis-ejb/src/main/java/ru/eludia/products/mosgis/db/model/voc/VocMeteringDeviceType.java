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
import ru.eludia.products.mosgis.db.model.tables.LivingRoom;
import ru.eludia.products.mosgis.db.model.tables.NonResidentialPremise;
import ru.eludia.products.mosgis.db.model.tables.ResidentialPremise;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi27;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi30;

public class VocMeteringDeviceType extends Table {
    
    private static final String TABLE_NAME = "vc_meter_types";
    
    public VocMeteringDeviceType () {       
        super (TABLE_NAME, "Типы приборов учёта");
        cols  (c.class);        
        pk    (c.ID);
        data  (i.class);
    }
    
    public enum c implements ColEnum {        
        
        ID             (Type.NUMERIC, 1,            "Идентификатор"),
        LABEL          (Type.STRING,                "Наименование"),
        IS_CONDO       (Type.BOOLEAN, Boolean.TRUE, "1 для МКД, 0 для ЖД"),
        CODE_VC_NSI_27 (Nsi27.class,                "Тип прибора учета (НСИ 27)"),
        CODE_VC_NSI_30 (Nsi30.class,  null,         "Характеристика помещения (НСИ 30)"),
        CLAZZ          (Type.STRING,  null,         "Класс")
        ;
        
        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}        
        
    }
    
    public enum i {
     
        APARTMENT_HOUSE         (1, 0, Nsi27.i.INDIVIDUAL,                                                            "ИПУ жилого дома"),
        COLLECTIVE              (2, 1, Nsi27.i.COLLECTIVE,                                                            "общедомовой ПУ"),
        NON_RESIDENTIAL_PREMISE (3, 1, Nsi27.i.INDIVIDUAL,           NonResidentialPremise.class,                     "ИПУ нежилого помещения"),
        RESIDENTIAL_PREMISE     (4, 1, Nsi27.i.INDIVIDUAL,           ResidentialPremise.class,    Nsi30.i.INDIVIDUAL, "ИПУ жилого помещения"),
        COLLECTIVE_APARTMENT    (5, 1, Nsi27.i.COLLECTIVE_APARTMENT, ResidentialPremise.class,    Nsi30.i.COLLECTIVE, "общеквартирный ПУ"),
        LIVING_ROOM             (6, 1, Nsi27.i.LIVING_ROOM,          LivingRoom.class,                                "комнатный ИПУ"),
        ;
                
        int     id;
        int     is_condo;
        int     code_vc_nsi_27;
        Integer code_vc_nsi_30 = null;
        String  clazz;
        String  label;

        public int getId () {
            return id;
        }

        public int getIs_condo () {
            return is_condo;
        }
        
        public String getLabel () {
            return label;
        }

        public String getClazz () {
            return clazz;
        }

        public int getCode_vc_nsi_27 () {
            return code_vc_nsi_27;
        }

        public Integer getCode_vc_nsi_30 () {
            return code_vc_nsi_30;
        }      

        private i (int id, int is_condo, Nsi27.i nsi27, String label) {
            this.id = id;
            this.is_condo = is_condo;
            this.code_vc_nsi_27 = nsi27.getId ();
            this.label = label;                    
        }
        
        private i (int id, int is_condo, Nsi27.i nsi27, Class clazz, String label) {
            this (id, is_condo, nsi27, label);
            this.clazz = clazz.getSimpleName ();
        }
        
        private i (int id, int is_condo, Nsi27.i nsi27, Class clazz, Nsi30.i nsi30, String label) {
            this (id, is_condo, nsi27, clazz, label);
            this.code_vc_nsi_30 = nsi30.getId ();
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