package ru.eludia.products.mosgis.jmx;

import javax.ejb.Local;
import javax.json.JsonObject;

public interface FiasMBean {
    
    public void   importFias (); 
    public String getProgressStatusText ();

}