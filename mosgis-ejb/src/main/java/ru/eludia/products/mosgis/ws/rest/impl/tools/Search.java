package ru.eludia.products.mosgis.ws.rest.impl.tools;

import java.util.logging.Logger;
import javax.json.JsonArray;
import javax.json.JsonObject;
import ru.eludia.base.db.sql.gen.Select;

public abstract class Search {
    
    private static final String SEARCH_LOGIC = "searchLogic";
    private static final String SEARCH = "search";
    protected final Logger logger = Logger.getLogger (this.getClass ().getName ());

    public final static Search from (JsonObject body) {
        
        if (!body.containsKey (SEARCH_LOGIC)) return null;
        
        final JsonArray terms = body.getJsonArray (SEARCH);
        
        switch (body.getString (SEARCH_LOGIC)) {
            case "OR": 
                return new SimpleSearch (terms.getJsonObject (0).getString ("value"));
            case "AND": 
                return new ComplexSearch (terms);
            default: 
                throw new IllegalArgumentException ("Unsupported " + SEARCH_LOGIC + ": " + body.getString (SEARCH_LOGIC));
        } 

    }
    
    public String getSearchString () {
        return null;
    }
    
    public abstract Select filter (Select s, String simpleSearchField);
    
}