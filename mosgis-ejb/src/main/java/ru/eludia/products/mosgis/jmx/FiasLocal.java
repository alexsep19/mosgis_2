package ru.eludia.products.mosgis.jmx;

import javax.ejb.Local;
import javax.json.JsonObject;

@Local
public interface FiasLocal {
    
    public void   importFias (); 
    public JsonObject getProgressStatus ();
    public String getProgressStatusText ();
    
}
