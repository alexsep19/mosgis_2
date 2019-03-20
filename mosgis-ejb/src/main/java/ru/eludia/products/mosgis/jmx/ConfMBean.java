package ru.eludia.products.mosgis.jmx;

import javax.ejb.Local;

@Local
public interface ConfMBean {
    
    public String getUrlBic ();
    public void   setUrlBic (String s);
    
    public String getPathOkei ();
    public void   setPathOkei (String s);
    
    public String getPathOktmo ();
    public void setPathOktmo (String s);
    
    public String getPathUnom ();
    public void setPathUnom (String s);

    public String getPathOksm();
    public void setPathOksm(String s);

    public String getPathFias ();
    public void   setPathFias (String s);
    
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
    
    public String getWsGisTariffsUrl ();
    public void   setWsGisTariffsUrl (String s);    
    public int    getWsGisTariffsConnTimeout ();     
    public void   setWsGisTariffsConnTimeout (int i);
    public int    getWsGisTariffsRespTimeout ();     
    public void   setWsGisTariffsRespTimeout (int i);    
    
    public String getWsGisLicensesUrl ();
    public void   setWsGisLicensesUrl (String s);    
    public int    getWsGisLicensesConnTimeout ();     
    public void   setWsGisLicensesConnTimeout (int i);
    public int    getWsGisLicensesRespTimeout ();     
    public void   setWsGisLicensesRespTimeout (int i);  

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

    public String getWsGisOrgCommonUrl ();
    public void   setWsGisOrgCommonUrl (String s);    
    public int    getWsGisOrgCommonConnTimeout ();     
    public void   setWsGisOrgCommonConnTimeout (int i);
    public int    getWsGisOrgCommonRespTimeout ();     
    public void   setWsGisOrgCommonRespTimeout (int i);    

    public String getWsGisOrgUrl ();
    public void   setWsGisOrgUrl (String s);    
    public int    getWsGisOrgConnTimeout ();     
    public void   setWsGisOrgConnTimeout (int i);
    public int    getWsGisOrgRespTimeout ();     
    public void   setWsGisOrgRespTimeout (int i);    

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
    
    public String getWsGisInfrastructureUrl ();
    public void   setWsGisInfrastructureUrl (String s);    
    public int    getWsGisInfrastructureConnTimeout ();     
    public void   setWsGisInfrastructureConnTimeout (int i);
    public int    getWsGisInfrastructureRespTimeout ();     
    public void   setWsGisInfrastructureRespTimeout (int i);
    
    public String getGisIdOrganization ();
    public void   setGisIdOrganization (String s);
    
    public String getWsGisUrlRoot ();
    public void   setWsGisUrlRoot (String s);
    
    public String getWsGisBasicLogin ();
    public void   setWsGisBasicLogin (String s);
    
    public String getWsGisBasicPassword ();    
    public void   setWsGisBasicPassword (String s);
    
    public int    getWsGisAsyncTtl ();
    public void   setWsGisAsyncTtl (int i);
    
    public String getWsGisServicesUrl ();
    public void   setWsGisServicesUrl (String s);    
    public int    getWsGisServicesConnTimeout ();     
    public void   setWsGisServicesConnTimeout (int i);
    public int    getWsGisServicesRespTimeout ();     
    public void   setWsGisServicesRespTimeout (int i);     

    public String getWsGisVolumeQualityUrl();
    public void setWsGisVolumeQualityUrl(String s);
    public int getWsGisVolumeQualityConnTimeout();
    public void setWsGisVolumeQualityConnTimeout(int i);
    public int getWsGisVolumeQualityRespTimeout();
    public void setWsGisVolumeQualityRespTimeout(int i);
    
    public String getWsGisMeteringUrl ();
    public void   setWsGisMeteringUrl (String s);    
    public int    getWsGisMeteringConnTimeout ();     
    public void   setWsGisMeteringConnTimeout (int i);
    public int    getWsGisMeteringRespTimeout ();     
    public void   setWsGisMeteringRespTimeout (int i);
    
    public String getWsGisInspectionUrl ();
    public void   setWsGisInspectionUrl (String s);    
    public int    getWsGisInspectionConnTimeout ();     
    public void   setWsGisInspectionConnTimeout (int i);
    public int    getWsGisInspectionRespTimeout ();     
    public void   setWsGisInspectionRespTimeout (int i);

    public String getWsGisUkUrl ();
    public void   setWsGisUkUrl (String s);
    public int    getWsGisUkConnTimeout ();
    public void   setWsGisUkConnTimeout (int i);
    public int    getWsGisUkRespTimeout ();
    public void   setWsGisUkRespTimeout (int i);
}