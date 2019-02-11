package ru.eludia.products.mosgis.web.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonNumber;
import javax.json.JsonValue;
import ru.eludia.base.db.sql.gen.Part;
import ru.eludia.base.db.sql.gen.Predicate;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.abs.Roster;
import ru.eludia.products.mosgis.ejb.ModelHolder;

public final class ComplexSearch extends Search {

    Map <String, Predicate> filters = new HashMap <> ();

    private String getOp (String s) {
        switch (s) {
            case "is":       return "=";
            case "less":     return "<=";
            case "more":     return ">=";
            case "between":  return "BETWEEN";
            case "begins":   return "LIKE ?%";
            case "ends":     return "LIKE %?";
            case "contains": return "LIKE %?%";
            case "in":       return "IN";
            case "not in":   return "NOT IN";
            case "null":     return "IS NULL";
            case "not null": return "IS NOT NULL";
            default:         throw new IllegalStateException (s + " operator not supported");
        }
    }

    public ComplexSearch (JsonArray terms) {

        for (JsonValue i: terms) {

            JsonObject o = (JsonObject) i;

            final JsonValue value = o.get ("value");

            Object [] values = new Object [] {};

            String op = getOp (o.getString ("operator"));

            if (value instanceof JsonString) {
                values = new Object [] {((JsonString) value).getString ().trim ().toUpperCase ()};
            }

            if (value instanceof JsonNumber) {
                values = new Object [] {((JsonNumber) value).bigDecimalValue ().toString ()};
            }

            if (value instanceof JsonArray) {
                    
                JsonArray a = (JsonArray) value;
                values = new Object [a.size ()];
                
                for (int j = 0; j < a.size (); j++) {
                    if (a.get (j) instanceof JsonString) {
                        values [j] = ((JsonString) a.get(j)).getString ();
                    } else {
                        JsonValue id = ((JsonObject) a.get (j)).get ("id");
                        values [j] = 
                            (id instanceof JsonNumber) ? ((JsonNumber) id).intValue () : 
                            ((JsonString) id).getString ();
                    }
                }
                
                for (Object oi: values) {
                    if ((oi instanceof Integer) && ((Integer) oi) != 0) continue;
                    if ((oi instanceof String) && !"0".equals (oi)) continue;
                    op = "..." + op;
                    break;
//                    if (((Integer) oi) == 0) op = "..." + op;
                }
            }
            
            filters.put (o.getString ("field"), new Predicate (op, values));

        }

    }

    public void apply (Select s) {
        
        Table table = s.getTable ();

        filters.forEach ((n, p) -> {
            final Col column = table.getColumn (n);
            if (column == null) {
                String [] colParts = n.split ("\\.");
                if (colParts.length == 2) {
                    Part part = s.getPart (colParts[0]);
                    if (part != null) part.and (colParts[1], p);
                    else logger.warning ("No alias '" + colParts[0] + "' in select. Filter ignored");
                }
                else logger.warning ("Column " + n + " not found in " + table.getName () + ". Filter ignored");
            }
            else s.and (n, p);
        });

    }

    @Override
    public Select filter (Select s, String simpleSearchField) {
        apply (s);
        return s;
    }

    public Map<String, Predicate> getFilters () {
        return filters;
    }

}