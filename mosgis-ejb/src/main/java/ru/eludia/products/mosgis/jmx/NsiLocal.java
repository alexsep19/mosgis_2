package ru.eludia.products.mosgis.jmx;

import javax.ejb.Local;
import ru.eludia.products.mosgis.db.model.voc.VocNsiListGroup;

@Local
public interface NsiLocal {
    
    public void importNsi ();     
    public void importNsiGroup (VocNsiListGroup.i group);
    public void importNsiItems (int registryNumber);
    public void importNsiItems (int registryNumber, Integer page);
    
}