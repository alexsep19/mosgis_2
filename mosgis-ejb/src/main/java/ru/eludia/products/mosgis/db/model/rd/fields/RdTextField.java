package ru.eludia.products.mosgis.db.model.rd.fields;

import javax.json.JsonObject;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Type;

public class RdTextField extends RdField {

    public RdTextField (JsonObject o) {
        super (o);
        col = new Col (name, Type.STRING, null, remark);
    }
    
}