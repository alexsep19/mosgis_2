package ru.eludia.products.mosgis.rest.providers;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class InternalServerErrorMapper implements ExceptionMapper<InternalServerErrorException>{

    private final static Logger logger = Logger.getLogger (InternalServerErrorMapper.class.getName ());

    @Override
    public Response toResponse (InternalServerErrorException exception) {
        
        String uuid = UUID.randomUUID ().toString ();
        
        logger.log (Level.SEVERE, uuid, exception);
        
        return Response.status (500).entity (
            Json.createObjectBuilder ()
                .add ("success", false)
                .add ("id", uuid)
            .build ()
        ).build ();
        
    }
    
}
