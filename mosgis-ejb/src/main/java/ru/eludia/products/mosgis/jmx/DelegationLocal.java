package ru.eludia.products.mosgis.jmx;

import javax.ejb.Local;

@Local
public interface DelegationLocal {
    
    public void   importAccessRequests (Integer page);

}