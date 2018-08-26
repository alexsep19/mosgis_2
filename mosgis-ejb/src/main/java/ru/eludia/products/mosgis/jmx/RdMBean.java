package ru.eludia.products.mosgis.jmx;

import javax.ejb.Local;

@Local
public interface RdMBean {
    
    public void importRdHouses ();
    public void importRdModelsTree ();
    public void importRdModel (int modelId);
    public void importRdIdNames (int modelId);

    public void importRdLostHouses ();
    public void importRdObjToHouses ();
    
}