package ru.eludia.products.mosgis.db.model.nsi.fields;

import java.util.List;
import java.util.Map;
import javax.json.JsonObject;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.abs.NamedObject;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementFieldType;

public abstract class NsiField extends NamedObject {
    
    boolean multiple;
            
    public NsiField (JsonObject o) {
        super (o.getString ("name"), o.getString ("remark"));
        multiple = o.getBoolean ("mul", false);
    }

    public final boolean isMultiple () {
        return multiple;
    }
            
    public static NsiField fromJson (JsonObject o) {
                
        final String type = o.getString ("type");                
        
        switch (type) {
            case "Boolean": 
                return new NsiBooleanField (o);
            case "Date": 
                return new NsiDateField (o);
            case "OkeiRef": 
                return new NsiOkeiRefField (o);
            case "String": 
                return new NsiStringField (o);
            case "Integer": 
                return new NsiIntegerField (o);
            case "NsiRef": 
                return new NsiNsiRefField (o);
            case "NsiListRef": 
                return new NsiNsiListRefField (o);
            case "Enum": 
                return new NsiEnumField (o);
            default: 
                throw new IllegalArgumentException (type + " type not supported");
        }
                
    }

    public abstract List<Col> getCols ();
    
    public abstract void add (Map<String, Object> record, NsiElementFieldType value);
    
}