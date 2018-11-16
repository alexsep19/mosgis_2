package ru.eludia.products.mosgis.rest;

import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.ServerErrorException;

public class ValidationException extends ServerErrorException {
    
    String fieldName;

    public String getFieldName () {
        return fieldName;
    }

    public ValidationException (String fieldName, String message) {
        super (message, 500);
        this.fieldName = fieldName;
    }
    
    private static final Pattern RE = Pattern.compile ("^#(\\w+)#: (.*)");
    
    public static ValidationException wrap (Exception ex) {
        
        if (!(ex instanceof SQLException)) return null;
        
        SQLException sqex = (SQLException) ex;
        
        if (sqex.getErrorCode () != 20000) return null;
        
        StringTokenizer st = new StringTokenizer (ex.getMessage (), "\n\r");
        
        String s = st.nextToken ().replace ("ORA-20000: ", "");
        
        Matcher matcher = RE.matcher (s);
        
        return matcher.matches () ? 
            new ValidationException (matcher.group (1), matcher.group (2)) :
            new ValidationException ("foo", s);
        
    }
        
}