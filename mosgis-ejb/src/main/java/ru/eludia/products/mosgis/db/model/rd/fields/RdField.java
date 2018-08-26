package ru.eludia.products.mosgis.db.model.rd.fields;

import javax.json.JsonObject;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.abs.NamedObject;
import ru.eludia.products.mosgis.db.model.voc.VocRdColType;

public abstract class RdField extends NamedObject {
    
    int id;
    Col col;

                
    public RdField (JsonObject o) {
        super ("F_" + o.getInt ("id"), o.getString ("remark"));
        this.id = o.getInt ("id");
    }
    
    public static RdField fromJson (JsonObject o) {

        final String type = o.getString ("type");                
        
        switch (VocRdColType.i.valueOf (type)) {
            case REF:
                return new RdRefField (o);
            case INT:
            case YEAR:
                return new RdIntField (o);
            case FLOAT:
            case TEXT:
                return new RdTextField (o);
            default: 
                throw new IllegalArgumentException (type + " type not supported");
        }

    }

    
//    public abstract void add (Map<String, Object> record, NsiElementFieldType value);

    public Col getCol () {
        return col;
    }
    
}