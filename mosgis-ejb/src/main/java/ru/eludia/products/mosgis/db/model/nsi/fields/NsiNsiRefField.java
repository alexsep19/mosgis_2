package ru.eludia.products.mosgis.db.model.nsi.fields;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.json.JsonObject;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Type;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementFieldType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementNsiRefFieldType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiRef;

public class NsiNsiRefField extends NsiScalarField {
    
    int registryNumber;
            
    public NsiNsiRefField (JsonObject o) {
        super (o);
        registryNumber = o.getInt ("ref");
        col = new Col (fName, Type.UUID, null, remark);
    }

    @Override
    public void add (Map<String, Object> record, NsiElementFieldType value) {
        final NsiElementNsiRefFieldType nr = (NsiElementNsiRefFieldType) value;
        if (nr == null) return;
        final NsiElementNsiRefFieldType.NsiRef nsiRef = nr.getNsiRef ();
        if (nsiRef == null) return;
        final NsiRef ref = nsiRef.getRef ();
        if (ref == null) return;
        final String guid = ref.getGUID ();
        if (guid == null) return;
        if (isMultiple ()) {
            if (!record.containsKey (fName)) record.put (fName, new ArrayList ());
            ((List) record.get (fName)).add (guid);
        }
        else {
            record.put (fName, guid);
        }
    }

    public final int getRegistryNumber () {
        return registryNumber;
    }
    
}