package ru.eludia.products.mosgis.jmx;

import javax.ejb.Local;

@Local
public interface OpenDataMBean {
    
    public void importOpenData ();

}