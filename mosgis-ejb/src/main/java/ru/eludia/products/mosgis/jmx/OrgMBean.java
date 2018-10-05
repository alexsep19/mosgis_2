package ru.eludia.products.mosgis.jmx;

import java.util.UUID;
import javax.ejb.Local;

@Local
public interface OrgMBean {
    
    public void importOrg (String ogrn);
    public void refreshOrg (UUID id);

}