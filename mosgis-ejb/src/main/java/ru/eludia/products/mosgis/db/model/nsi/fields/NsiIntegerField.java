package ru.eludia.products.mosgis.db.model.nsi.fields;

import java.util.Map;
import javax.json.JsonObject;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Type;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementFieldType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementIntegerFieldType;

public class NsiIntegerField extends NsiScalarField {
            
    public NsiIntegerField (JsonObject o) {
        super (o);
        col = new Col (fName, Type.INTEGER, null, remark);
    }

    @Override
    public void add (Map<String, Object> record, NsiElementFieldType value) {
        record.put (fName, ((NsiElementIntegerFieldType) value).getValue ());
    }
    
}