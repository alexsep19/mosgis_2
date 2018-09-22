package ru.eludia.products.mosgis.db.model.nsi.fields;

import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;
import javax.json.JsonObject;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementFieldType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementNsiRefFieldType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiRef;

public class NsiNsiListRefField extends NsiScalarField {
            
    public NsiNsiListRefField (JsonObject o) {
        super (o);
        col = new Col (fName, Type.INTEGER, null, remark);
    }

    @Override
    public void add (Map<String, Object> record, NsiElementFieldType value) {
        final NsiElementNsiRefFieldType nr = (NsiElementNsiRefFieldType) value;
        if (nr == null) return;
        final NsiElementNsiRefFieldType.NsiRef nsiRef = nr.getNsiRef ();
        if (nsiRef == null) return;
        record.put (fName, nsiRef.getNsiItemRegistryNumber ());
    }
    
    @Override
    public NsiElementFieldType toDom (Object value) throws java.sql.SQLException {
        if (value == null) return null;
        NsiElementNsiRefFieldType result = new NsiElementNsiRefFieldType ();
        result.setName (remark);
        final NsiElementNsiRefFieldType.NsiRef nsiRef = new NsiElementNsiRefFieldType.NsiRef ();
        result.setNsiRef (nsiRef);
        nsiRef.setNsiItemRegistryNumber (BigInteger.valueOf (Long.parseLong (value.toString ())));
        nsiRef.setRef (NsiTable.toDom (value.toString (), UUID.randomUUID ()));
        return result;
    }

}