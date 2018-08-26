package ru.eludia.products.mosgis.db.model.nsi.fields;

import java.util.Map;
import javax.json.JsonObject;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Type;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementFieldType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementDateFieldType;

public class NsiDateField extends NsiScalarField {
            
    public NsiDateField (JsonObject o) {
        super (o);
        col = new Col (fName, Type.DATE, null, remark);
    }

    @Override
    public void add (Map<String, Object> record, NsiElementFieldType value) {
        record.put (fName, ((NsiElementDateFieldType) value).getValue ());
    }
    
}