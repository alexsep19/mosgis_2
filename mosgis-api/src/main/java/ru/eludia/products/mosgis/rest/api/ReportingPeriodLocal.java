package ru.eludia.products.mosgis.rest.api;

import javax.ejb.Local;
import javax.json.JsonObject;
import ru.eludia.products.mosgis.rest.User;

@Local
public interface ReportingPeriodLocal {    
    
    JsonObject getItem  (String id);   
    JsonObject doFill   (String id, User user);
    
}