package ru.eludia.products.mosgis.jmx;

import javax.ejb.Local;

@Local
public interface ExportHouseMBean {

    public void exportHouseData(String fiasHouseGuid);

}
