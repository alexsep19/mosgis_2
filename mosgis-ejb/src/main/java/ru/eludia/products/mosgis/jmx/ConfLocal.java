package ru.eludia.products.mosgis.jmx;

import javax.ejb.Local;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;

@Local
public interface ConfLocal {
    
    public void set (VocSetting.i key, String value);
    public void reload ();
    
}