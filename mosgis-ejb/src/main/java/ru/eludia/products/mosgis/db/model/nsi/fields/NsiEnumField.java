package ru.eludia.products.mosgis.db.model.nsi.fields;

import java.util.List;
import java.util.StringTokenizer;
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
    
    @Override
    public NsiElementFieldType toDom (Object value) throws java.sql.SQLException {
        
        if (value == null) return null;
        
        NsiElementEnumFieldType result = new NsiElementEnumFieldType ();
        result.setName (remark);
        List<NsiElementEnumFieldType.Position> position = result.getPosition ();
        StringTokenizer st = new StringTokenizer (value.toString (), "|");
        while (st.hasMoreTokens ()) {
            String t = st.nextToken ();
            final NsiElementEnumFieldType.Position p = new NsiElementEnumFieldType.Position ();
            p.setValue (t);
            p.setGUID (t);
            position.add (p);
        }
        return result;
        
    }

}