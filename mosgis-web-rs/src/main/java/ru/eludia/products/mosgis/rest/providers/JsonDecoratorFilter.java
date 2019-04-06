package ru.eludia.products.mosgis.rest.providers;

import java.io.IOException;
import javax.annotation.Priority;
import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import static javax.ws.rs.core.Response.Status.OK;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.USER)
public class JsonDecoratorFilter implements ContainerResponseFilter {
    
    @Context
    HttpServletRequest httpServletRequest;    

    @Override
    public void filter (ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        
        if (OK.equals (responseContext.getStatusInfo ())) {
            
            if (!(responseContext.getEntity () instanceof JsonObject)) return;

            responseContext.setEntity (Json.createObjectBuilder ()
                .add ("success", true)
                .add ("content", (JsonObject) responseContext.getEntity ())
                .build ()
            );
            
        }

    }
    
}