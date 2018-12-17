package ru.eludia.products.mosgis.jmx;

import javax.ejb.Local;

@Local
public interface HouseMBean {

    public void exportHouseData(String fiasHouseGuid);
    
    public void importHouseData(String uuid, String orgPPAGuid);
}
