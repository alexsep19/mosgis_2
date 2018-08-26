package ru.eludia.products.mosgis.db.model.nsi.fields;

import javax.json.JsonObject;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementFieldType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementOkeiRefFieldType;

public class NsiOkeiRefField extends NsiStringField {
            
    public NsiOkeiRefField (JsonObject o) {
        super (o);
    }

    String getString (NsiElementFieldType value) {
        return ((NsiElementOkeiRefFieldType) value).getCode ();
    }

}