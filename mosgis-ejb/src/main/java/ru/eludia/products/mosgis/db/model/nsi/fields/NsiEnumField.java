package ru.eludia.products.mosgis.db.model.nsi.fields;

import java.util.stream.Collectors;
import javax.json.JsonObject;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementFieldType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementEnumFieldType;

public class NsiEnumField extends NsiStringField {
            
    public NsiEnumField (JsonObject o) {
        super (o);
    }

    String getString (NsiElementFieldType value) {
        return ((NsiElementEnumFieldType) value)
            .getPosition ().stream ()
            .map (i -> i.getValue ())
            .collect (Collectors.joining ("|"));
    }

}