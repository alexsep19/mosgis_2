package ru.eludia.products.mosgis.jmx;

import javax.ejb.Local;

@Local
public interface DiffMBean {
    
    public void importDiff ();

}