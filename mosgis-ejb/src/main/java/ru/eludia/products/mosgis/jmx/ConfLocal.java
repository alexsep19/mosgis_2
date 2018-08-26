package ru.eludia.products.mosgis.jmx;

import javax.ejb.Local;

@Local
public interface ConfLocal {
    
    public void set (String key, String value);
    public void reload ();
    
}