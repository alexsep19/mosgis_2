package ru.eludia.products.mosgis.db.model.nsi.fields;

import java.util.Map;
import javax.json.JsonObject;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Type;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementFieldType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementStringFieldType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementOkeiRefFieldType;

public class NsiStringField extends NsiScalarField {
            
    public NsiStringField (JsonObject o) {
        super (o);
        col = new Col (fName, Type.STRING, null, remark);
    }

    @Override
    public void add (Map<String, Object> record, NsiElementFieldType value) {
        record.put (fName, getString (value));
    }

    String getString (NsiElementFieldType value) {
        if (value instanceof NsiElementOkeiRefFieldType) return ((NsiElementOkeiRefFieldType) value).getCode ();
        return ((NsiElementStringFieldType) value).getValue ();
    }
    
}