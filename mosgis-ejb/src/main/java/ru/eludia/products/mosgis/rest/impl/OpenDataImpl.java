package ru.eludia.products.mosgis.rest.impl;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.build.QP;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.rest.api.OpenDataLocal;
import ru.eludia.products.mosgis.db.model.incoming.InOpenData;
import ru.eludia.products.mosgis.db.model.incoming.InOpenDataLine;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.web.base.Search;

@Stateless
public class OpenDataImpl implements OpenDataLocal {

    private static final Logger logger = Logger.getLogger (OpenDataImpl.class.getName ());
    
    private static final Pattern RE_KAD_N = Pattern.compile ("77:\\d{2}:\\d{7}:\\d{4}");

    private Select setFilter (Select select, String term) {
        
        if (term == null) return select;
        
        if ("DUP".equals (term)) return select
            .where ("fiashouseguid IN", new QP ("SELECT fiashouseguid FROM in_open_data_lines WHERE fiashouseguid IS NOT NULL GROUP BY fiashouseguid HAVING COUNT(*)>1"))
            .orderBy ("fiashouseguid");
        
        if ("NO GUID".equals (term)) return select
            .where ("fiashouseguid IS NULL");
        
        try {
            return select
                .where ("unom", Integer.valueOf (term));
        }
        catch (NumberFormatException e) {
        }
        
        try {
            return select
                .where ("fiashouseguid", UUID.fromString (term));
        }
        catch (IllegalArgumentException e) {
        }
        
        if (RE_KAD_N.matcher (term).matches ()) return select
            .where ("kad_n", term);
        
        return select
            .where ("address_uc LIKE %?%", String.join ("%", term.split (" +")));
                
    }
    
    @Override
    public JsonObject select (JsonObject p) {
        
        Select select = ModelHolder.getModel ().select (InOpenDataLine.class, 
            "unom AS id",
            "fiashouseguid",
            "address",
            "kad_n",
            "fn",
            "is_actual",
            "line"
        )                
        .and ("is_actual", 1)                                        
        .limit (p.getInt ("offset"), p.getInt ("limit"));
        
        final Search search = Search.from (p);
        
        if (search != null) select = setFilter (select, search.getSearchString ().toUpperCase ());

        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {
            db.addJsonArrayCnt (jb, select.orderBy ("address"));
        }
        catch (SQLException ex) {
            throw new InternalServerErrorException (ex);
        }

        return jb.build ();

    }

    @Override
    public JsonObject getLog (JsonObject p) {
        
        JsonObjectBuilder jb = Json.createObjectBuilder ();

        try (DB db = ModelHolder.getModel ().getDb ()) {

            db.addJsonArrayCnt (jb, ModelHolder.getModel ().select (InOpenData.class, 
                "uuid AS id",
                "dt",
                "no",
                "dt_from",
                "dt_to_fact",
                "sz",
                "rd"                                
            )
            .orderBy ("dt_from DESC")
            .limit (p.getInt ("offset"), p.getInt ("limit")));

        }
        catch (SQLException ex) {
            throw new InternalServerErrorException (ex);
        }

        return jb.build ();
        
    }
    
}