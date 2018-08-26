package ru.eludia.products.mosgis.db.model.rd.fields;

import javax.json.JsonObject;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Type;

public class RdIntField extends RdField {

    public RdIntField (JsonObject o) {
        super (o);
        col = new Col (name, Type.INTEGER, null, remark);
    }
    
}