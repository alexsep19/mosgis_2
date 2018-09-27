package ru.eludia.products.mosgis.jmx;

public interface NsiMBean {
    
    public void importNsi ();
    public void importNsiItems (int registryNumber);
    public void checkForPending ();
    public String getProgressStatusText ();
    
}