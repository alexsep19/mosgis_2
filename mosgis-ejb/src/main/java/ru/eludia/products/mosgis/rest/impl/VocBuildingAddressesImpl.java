package ru.eludia.products.mosgis.rest.impl;

import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.ejb.Stateless;
import javax.json.JsonObject;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.voc.VocBuildingAddress;
import ru.eludia.products.mosgis.rest.api.VocBuildingAddressesLocal;
import ru.eludia.products.mosgis.rest.impl.base.Base;

@Stateless
public class VocBuildingAddressesImpl extends Base implements VocBuildingAddressesLocal {

    private static final Logger logger = Logger.getLogger (VocBuildingAddressesImpl.class.getName ());
    
    private static final Pattern RE_ZIP = Pattern.compile ("[1-9][0-9]{5}");

    @Override
    public JsonObject select (JsonObject p) {return fetchData ((db, job) -> {
        
        Select select = db.getModel ()
            .select  (VocBuildingAddress.class, "houseguid AS id", "label", "postalcode")
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
                sb.append (token.toUpperCase ());
                sb.append ('%');
            }
        }
       
        if (sb.length () > 0) {
            String value = sb.toString();
            if (value.contains("Ё") || value.contains ("ё")) {
                    select.and ("label_uc LIKE", value.replace ("Ё", "Е").replace ("ё", "е"));
                } else {
                    select.and ("label_uc LIKE", value);
                }
        }
        
        db.addJsonArrays (job, select);

    });}

}