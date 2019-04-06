package ru.eludia.products.mosgis.rest.providers;

import java.util.logging.Logger;
import javax.json.Json;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import ru.eludia.products.mosgis.rest.ValidationException;

@Provider
public class ValidationErrorMapper implements ExceptionMapper<ValidationException>{

    private final static Logger logger = Logger.getLogger (ValidationErrorMapper.class.getName ());

    @Override
    public Response toResponse (ValidationException exception) {
                
        return Response.status (422).entity (
            Json.createObjectBuilder ()
                .add ("success", false)
                .add ("field", exception.getFieldName ())
                .add ("message", exception.getMessage ())
            .build ()
        ).build ();
        
    }
    
}