package ru.eludia.products.mosgis.web.base;

import ru.eludia.base.db.sql.gen.Select;

public final class SimpleSearch extends Search {
    
    String term;

    public SimpleSearch (String s) {
        term = s.trim ();
        if (s.isEmpty ()) term = null;
    }

    @Override
    public String getSearchString () {
        return term;
    }

    @Override
    public Select filter (Select s, String simpleSearchField) {
        return s.and (simpleSearchField, getSearchString ().toUpperCase ());
    }
    
}