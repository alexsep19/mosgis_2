package ru.eludia.products.mosgis.ws.rest.impl;

import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.rest.api.VocBuildingAddressesLocal;
import ru.eludia.products.mosgis.ws.rest.impl.base.Base;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class VocBuildingAddressesImpl extends Base implements VocBuildingAddressesLocal {

    private static final Logger logger = Logger.getLogger (VocBuildingAddressesImpl.class.getName ());
    
    private static final Pattern RE_ZIP = Pattern.compile ("[1-9][0-9]{5}");

    @Override
    public JsonObject select (JsonObject p) {return fetchData ((db, job) -> {
        
        Select select = db.getModel ()
            .select  (VocBuilding.class, "houseguid AS id", "label", "postalcode")
            .orderBy ("postalcode, label")
            .limit (0, 50);

        StringBuilder sb = new StringBuilder ();
        StringTokenizer st = new StringTokenizer (p.getString ("search", ""));

        while (st.hasMoreTokens ()) {
            
            final String token = st.nextToken ();
            
            if (sb.length () == 0 && RE_ZIP.matcher (token).matches ()) {
                select.and ("postalcode", token);
            }
            else {
                sb.append (token.toUpperCase ().replace ('Ё', 'Е'));
                sb.append ('%');
            }
        }
        
        if (sb.length () > 0) {
            select.and ("label_uc LIKE", sb.toString ());
        }
        
        db.addJsonArrays (job, select);

    });}

}