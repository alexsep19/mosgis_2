package ru.eludia.products.mosgis.db.model.rd.fields;

import javax.json.JsonObject;

public class RdRefField extends RdIntField {
    
    int ref;

    public RdRefField (JsonObject o) {
        super (o);
        ref = o.getInt ("ref");
    }
    
}