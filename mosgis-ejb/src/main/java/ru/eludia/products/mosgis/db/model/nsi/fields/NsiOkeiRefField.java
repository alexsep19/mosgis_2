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
    
    @Override
    public NsiElementFieldType toDom (Object value) throws java.sql.SQLException {
        if (value == null) return null;
        NsiElementOkeiRefFieldType result = new NsiElementOkeiRefFieldType ();
        result.setName (remark);
        result.setCode (value.toString ());
        return result;
    }

}