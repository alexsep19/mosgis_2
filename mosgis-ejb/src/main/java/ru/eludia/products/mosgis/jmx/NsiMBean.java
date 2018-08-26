package ru.eludia.products.mosgis.jmx;

import javax.ejb.Local;

@Local
public interface NsiMBean {
    
    public void importNsi ();     
    public void importNsiItems (int registryNumber);
    
}