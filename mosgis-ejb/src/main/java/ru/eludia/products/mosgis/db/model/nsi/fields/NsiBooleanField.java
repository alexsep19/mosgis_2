package ru.eludia.products.mosgis.db.model.nsi.fields;

import java.util.Map;
import javax.json.JsonObject;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Type;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementFieldType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementBooleanFieldType;

public final class NsiBooleanField extends NsiScalarField {
           
    public NsiBooleanField (JsonObject o) {
        super (o);
        col = new Col (fName, Type.BOOLEAN, null, remark);
    }

    @Override
    public void add (Map<String, Object> record, NsiElementFieldType value) {
        NsiElementBooleanFieldType v = (NsiElementBooleanFieldType) value;
        if (v == null) return;
        Boolean b = v.isValue ();
        record.put (fName, b == null ? null : b ? 1 : 0);
    }
    
}