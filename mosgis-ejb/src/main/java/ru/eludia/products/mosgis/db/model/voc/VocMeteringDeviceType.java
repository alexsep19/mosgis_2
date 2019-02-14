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

public class VocMeteringDeviceType extends Table {
    
    private static final String TABLE_NAME = "vc_meter_types";
    
    public VocMeteringDeviceType () {       
        super (TABLE_NAME, "Типы приборов учёта");
        cols  (c.class);        
        pk    (c.ID);
        data  (i.class);
    }
    
    public enum c implements ColEnum {        
        
        ID             (Type.NUMERIC, 1,    "Идентификатор"),
        LABEL          (Type.STRING,        "Наименование"),
        CODE_VC_NSI_27 (Nsi27.class,        "Тип прибора учета (НСИ 27)"),
        CLAZZ          (Type.STRING,  null, "Класс")
        ;
        
        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}        
        
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
        APARTMENT_HOUSE         (1, Nsi27.i.INDIVIDUAL,                                        "ИПУ жилого дома"),
        COLLECTIVE              (2, Nsi27.i.COLLECTIVE,                                        "общедомовой ПУ"),       
        NON_RESIDENTIAL_PREMISE (3, Nsi27.i.INDIVIDUAL,           NonResidentialPremise.class, "ИПУ нежилого помещения"),
        RESIDENTIAL_PREMISE     (4, Nsi27.i.INDIVIDUAL,           ResidentialPremise.class,    "ИПУ жилого помещения"),
        COLLECTIVE_APARTMENT    (5, Nsi27.i.COLLECTIVE_APARTMENT, ResidentialPremise.class,    "общеквартирный ПУ"),
        LIVING_ROOM             (6, Nsi27.i.LIVING_ROOM,          LivingRoom.class,            "комнатный ИПУ"),
        ;
                
        int    id;
        int    code_vc_nsi_27;
        String clazz;
        String label;

        public int getId () {
            return id;
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

        private i (int id, Nsi27.i nsi27, String label) {
            this.id = id;
            this.code_vc_nsi_27 = nsi27.getId ();
            this.label = label;                    
        }
        
        private i (int id, Nsi27.i nsi27, Class clazz, String label) {
            this (id, nsi27, label);
            this.clazz = clazz.getSimpleName ();
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