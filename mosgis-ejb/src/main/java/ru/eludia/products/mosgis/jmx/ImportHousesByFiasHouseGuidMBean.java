package ru.eludia.products.mosgis.jmx;

import javax.ejb.Local;

@Local
public interface ImportHousesByFiasHouseGuidMBean {
    
    public String getState ();
    
    public void start ();
    public void stop ();
    
}