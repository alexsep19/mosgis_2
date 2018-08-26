package ru.eludia.products.mosgis.jmx;

import javax.ejb.Local;

@Local
public interface ConfMBean {
    
    public String getPathOkei ();
    public void   setPathOkei (String s);

    public String getPathFias ();
    public void   setPathFias (String s);

    public String getPathOpenData ();
    public void   setPathOpenData (String s);

    public String getWsGisFilesUrl ();
    public void   setWsGisFilesUrl (String s);    
    public int    getWsGisFilesConnTimeout ();     
    public void   setWsGisFilesConnTimeout (int i);
    public int    getWsGisFilesRespTimeout ();     
    public void   setWsGisFilesRespTimeout (int i);

    public String getWsGisBillsUrl ();
    public void   setWsGisBillsUrl (String s);    
    public int    getWsGisBillsConnTimeout ();     
    public void   setWsGisBillsConnTimeout (int i);
    public int    getWsGisBillsRespTimeout ();     
    public void   setWsGisBillsRespTimeout (int i);

    public String getWsGisNsiUrl ();
    public void   setWsGisNsiUrl (String s);    
     
    public int    getWsGisNsiConnTimeout ();     
    public void   setWsGisNsiConnTimeout (int i);
    public int    getWsGisNsiRespTimeout ();     
    public void   setWsGisNsiRespTimeout (int i);

    public String getWsGisNsiCommonUrl ();
    public void   setWsGisNsiCommonUrl (String s);    
    public int    getWsGisNsiCommonConnTimeout ();     
    public void   setWsGisNsiCommonConnTimeout (int i);
    public int    getWsGisNsiCommonRespTimeout ();     
    public void   setWsGisNsiCommonRespTimeout (int i);
/*    
    public String getWsGisRdUrl ();
    public void   setWsGisRdUrl (String s);
    public int    getWsGisRdConnTimeout ();     
    public void   setWsGisRdConnTimeout (int i);
    public int    getWsGisRdRespTimeout ();     
    public void   setWsGisRdRespTimeout (int i);
*/
    public String getWsGisOrgCommonUrl ();
    public void   setWsGisOrgCommonUrl (String s);    
    public int    getWsGisOrgCommonConnTimeout ();     
    public void   setWsGisOrgCommonConnTimeout (int i);
    public int    getWsGisOrgCommonRespTimeout ();     
    public void   setWsGisOrgCommonRespTimeout (int i);    

    public String getUserAdminLogin ();
    public void   setUserAdminLogin (String s);
    public String getUserAdminPassword ();
    public void   setUserAdminPassword (String s);

    public String getWsGisHouseManagementUrl ();
    public void   setWsGisHouseManagementUrl (String s);    
    public int    getWsGisHouseManagementConnTimeout ();     
    public void   setWsGisHouseManagementConnTimeout (int i);
    public int    getWsGisHouseManagementRespTimeout ();     
    public void   setWsGisHouseManagementRespTimeout (int i);  

}