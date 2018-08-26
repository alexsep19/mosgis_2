package ru.eludia.products.mosgis.jmx;

import javax.ejb.Local;

@Local
public interface OrgMBean {
    
    public void importOrg (String ogrn);

}