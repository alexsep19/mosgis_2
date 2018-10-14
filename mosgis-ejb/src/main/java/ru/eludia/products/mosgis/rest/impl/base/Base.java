package ru.eludia.products.mosgis.rest.impl.base;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.InternalServerErrorException;
import ru.eludia.base.DB;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.tables.Passport;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.ValidationException;

public abstract class Base <T extends Table> {

    final protected Logger logger = Logger.getLogger (getClass ().getName ());
    
    public static final JsonObject EMPTY_JSON_OBJECT = Json.createObjectBuilder ().build ();
    
    public final Table getTable () throws SQLException {
        Class c = getClass ();
        Class sc = c.getSuperclass ();
        Type t = sc.getGenericSuperclass ();
        final Table table = ModelHolder.getModel ().get ((Class) ((ParameterizedType) t).getActualTypeArguments () [0]);
        if (table instanceof Passport) {
            try (DB db = ModelHolder.getModel ().getDb ()) {
                ((Passport) table).addNsiFields (db);
            }
        }
        return table;
    }    
    
    @FunctionalInterface
    public interface ActionHandler {
        void accept (DB db) throws Exception;
    }
    
    @FunctionalInterface
    public interface DataFetcher {
        void accept (DB db, JsonObjectBuilder job) throws Exception;
    }
    
    public Map<String, Object> getData (JsonObject p, Object... o) throws SQLException {
        return getTable ().HASH (p.getJsonObject ("data"), o);
    }
    
    private static final Pattern re = Pattern.compile ("^#(\\w+)#: (.*)");

    public JsonObject doAction (ActionHandler h) {
       
        try (DB db = ModelHolder.getModel ().getDb ()) {            
            db.begin ();
            h.accept (db);
            db.commit ();
        }
        catch (ValidationException ex) {
            throw ex;
        }
        catch (SQLException ex) {

            if (ex.getErrorCode () == 20000) {                
                StringTokenizer st = new StringTokenizer (ex.getMessage (), "\n\r");
                String s = st.nextToken ().replace ("ORA-20000: ", "");
                Matcher matcher = re.matcher (s);
                throw matcher.matches () ? 
                    new ValidationException (matcher.group (1), matcher.group (2)) :
                    new ValidationException ("foo", s);
            }
            else {
                throw new InternalServerErrorException (ex);
            }
                
        }
        catch (Exception ex) {
            
            Throwable t = ex;
            
            while (true) {
                Throwable c = t.getCause ();
                if (c == null) throw new InternalServerErrorException (t);
                t = c;
            }
            
        }
        
        return EMPTY_JSON_OBJECT;

    }
    
    public JsonObject fetchData (DataFetcher h) {
        
        JsonObjectBuilder job = Json.createObjectBuilder ();
       
        try (DB db = ModelHolder.getModel ().getDb ()) {            
            h.accept (db, job);
        }
        catch (ValidationException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new InternalServerErrorException (ex);
        }
        
        return job.build ();

    }

}