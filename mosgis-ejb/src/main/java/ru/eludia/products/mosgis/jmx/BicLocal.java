package ru.eludia.products.mosgis.jmx;

import javax.ejb.Local;
import ru.eludia.products.mosgis.rest.User;

@Local
public interface BicLocal {
    
    public void importBic (User user);

}