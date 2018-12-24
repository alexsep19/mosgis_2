package ru.eludia.products.mosgis.jmx;

import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Topic;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.products.mosgis.db.model.tables.Setting;
import ru.eludia.products.mosgis.db.model.tables.SettingValue;
import ru.eludia.products.mosgis.db.model.voc.VocSetting;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.ejb.UUIDPublisher;

@Startup
@Singleton
@DependsOn ("ModelHolder")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class Conf implements ConfMBean, ConfLocal {

    private ObjectName objectName = null;
    private MBeanServer platformMBeanServer;
    private static Logger logger = Logger.getLogger (Conf.class.getName ());
    
    @EJB
    protected UUIDPublisher UUIDPublisher;

    @Resource (mappedName = "mosgis.confTopic")
    Topic confTopic;
    
    @PostConstruct
    public void registerInJMX () {
        
        reload ();

        try {
            objectName = new ObjectName ("ru.eludia:Name=MosGis,Type=Conf");
            platformMBeanServer = ManagementFactory.getPlatformMBeanServer ();
            platformMBeanServer.registerMBean (this, objectName);
        } 
        catch (Exception e) {
            throw new IllegalStateException ("Problem during registration of Monitoring into JMX:" + e);
        }

    }

    @PreDestroy
    public void unregisterFromJMX () {

        try {
            platformMBeanServer.unregisterMBean (this.objectName);
        } catch (Exception e) {
            throw new IllegalStateException ("Problem during unregistration of Monitoring into JMX:" + e);
        }

    }
    
    private static ConcurrentHashMap <String, String> settings = new ConcurrentHashMap<String, String> ();    
       
    public static String get (VocSetting.i key) {
        final String s = settings.get (key.getId ());
        return s == null ? "" : s;
    }
    
    public static int getInt (VocSetting.i key) {
        try {
            return Integer.valueOf (get (key));
        }
        catch (Exception e) {
            return 0;
        }
    }
    
    private void set (VocSetting.i key, String value) {
        set (key.getId (), value);
    }
    
    private void setInt (VocSetting.i key, int value) {
        set (key.getId (), String.valueOf (value));
    }
    
    @Override
    public void set (String key, String value) {

        try (DB db = ModelHolder.getModel ().getDb ()) {

            db.upsert (SettingValue.class, HASH (
                "id_vc_setting", key,
                "value",         value
            ));
            
            UUIDPublisher.publish (confTopic, UUID.fromString ("00000000-0000-0000-0000-000000000000"));

        }
        catch (SQLException ex) {
            throw new IllegalStateException (ex);
        }

    }

    @Override
    public void reload () {
        
        try (DB db = ModelHolder.getModel ().getDb ()) {
            
            db.forEach (ModelHolder.getModel ().select (Setting.class, "id", "value"), rs -> {
           
                final String id = rs.getString ("id");                
                final String value = rs.getString ("value");
                
                settings.put (id, value == null ? "" : value);
  
                logger.log (Level.INFO, "Loaded {0}", id);
                
            });
            
        }
        catch (Exception ex) {
            throw new IllegalStateException (ex);
        }
        
    }

    @Override
    public String getPathFias () {
        return get (VocSetting.i.PATH_FIAS);
    }

    @Override
    public void setPathFias (String s) {
        
//        Fias.CheckPath ch = new Fias.CheckPath(FileSystems.getDefault ().getPath (Conf.get (VocSetting.i.PATH_FIAS)));
//        ch.check();
        set (VocSetting.i.PATH_FIAS, s);
    }

    @Override
    public String getPathOpenData () {
        return get (VocSetting.i.PATH_OPENDATA);
    }

    @Override
    public void setPathOpenData (String s) {
        set (VocSetting.i.PATH_OPENDATA, s);
    }
   
    @Override
    public String getWsGisNsiCommonUrl () {
        return get (VocSetting.i.WS_GIS_NSI_COMMON_URL);
    }

    @Override
    public void setWsGisNsiCommonUrl (String s) {
        set (VocSetting.i.WS_GIS_NSI_COMMON_URL, s);
        set (VocSetting.i.WS_GIS_URL_ROOT, "");
    }

    @Override
    public int getWsGisNsiCommonConnTimeout () {
        return getInt (VocSetting.i.WS_GIS_NSI_COMMON_TMT_CONN);
    }

    @Override
    public void setWsGisNsiCommonConnTimeout (int i) {
        setInt (VocSetting.i.WS_GIS_NSI_COMMON_TMT_CONN, i);
    }

    @Override
    public int getWsGisNsiCommonRespTimeout () {
        return getInt (VocSetting.i.WS_GIS_NSI_COMMON_TMT_RESP);
    }

    @Override
    public void setWsGisNsiCommonRespTimeout (int i) {
        setInt (VocSetting.i.WS_GIS_NSI_COMMON_TMT_RESP, i);
    }

    @Override
    public String getPathOkei () {
        return get (VocSetting.i.PATH_OKEI);
    }

    @Override
    public void setPathOkei (String s) {
        set (VocSetting.i.PATH_OKEI, s);
    }
    
    @Override
    public String getPathOksm() {
        return get(VocSetting.i.PATH_OKSM);
    }

    @Override
    public void setPathOksm(String s) {
        set(VocSetting.i.PATH_OKSM, s);
    }

    @Override
    public String getPathOktmo () {
        return get (VocSetting.i.PATH_OKTMO);
    }
    
    @Override
    public void setPathOktmo (String s) {
        set (VocSetting.i.PATH_OKTMO, s);
    }

    @Override
    public String getWsGisOrgCommonUrl () {
        return get (VocSetting.i.WS_GIS_ORG_COMMON_URL);
    }

    @Override
    public void setWsGisOrgCommonUrl (String s) {
        set (VocSetting.i.WS_GIS_ORG_COMMON_URL, s);
        set (VocSetting.i.WS_GIS_URL_ROOT, "");
    }

    @Override
    public int getWsGisOrgCommonConnTimeout () {
        return getInt (VocSetting.i.WS_GIS_ORG_COMMON_TMT_CONN);
    }

    @Override
    public void setWsGisOrgCommonConnTimeout (int i) {
        setInt (VocSetting.i.WS_GIS_ORG_COMMON_TMT_CONN, i);
    }

    @Override
    public int getWsGisOrgCommonRespTimeout () {
        return getInt (VocSetting.i.WS_GIS_ORG_COMMON_TMT_RESP);
    }

    @Override
    public void setWsGisOrgCommonRespTimeout (int i) {
        setInt (VocSetting.i.WS_GIS_ORG_COMMON_TMT_RESP, i);
    }

    @Override
    public String getUserAdminLogin () {
        return get (VocSetting.i.USER_ADMIN_LOGIN);
    }

    @Override
    public void setUserAdminLogin (String s) {
        set (VocSetting.i.USER_ADMIN_LOGIN, s);
    }

    @Override
    public String getUserAdminPassword () {
        return "******";
    }

    @Override
    public void setUserAdminPassword (String s) {
        set (VocSetting.i.USER_ADMIN_PASSWORD, s);
    }
        
    @Override
    public String getWsGisNsiUrl () {
        return get (VocSetting.i.WS_GIS_NSI_URL);
    }

    @Override
    public void setWsGisNsiUrl (String s) {
        set (VocSetting.i.WS_GIS_NSI_URL, s);
        set (VocSetting.i.WS_GIS_URL_ROOT, "");
    }

    @Override
    public int getWsGisNsiConnTimeout () {
        return getInt (VocSetting.i.WS_GIS_NSI_TMT_CONN);
    }

    @Override
    public void setWsGisNsiConnTimeout (int i) {
        setInt (VocSetting.i.WS_GIS_NSI_TMT_CONN, i);
    }

    @Override
    public int getWsGisNsiRespTimeout () {
        return getInt (VocSetting.i.WS_GIS_NSI_TMT_RESP);
    }

    @Override
    public void setWsGisNsiRespTimeout (int i) {
        setInt (VocSetting.i.WS_GIS_NSI_TMT_RESP, i);
    }

    @Override
    public String getWsGisBillsUrl () {
        return get (VocSetting.i.WS_GIS_BILLS_URL);
    }

    @Override
    public void setWsGisBillsUrl (String s) {
        set (VocSetting.i.WS_GIS_BILLS_URL, s);
        set (VocSetting.i.WS_GIS_URL_ROOT, "");
    }

    @Override
    public int getWsGisBillsConnTimeout () {
        return getInt (VocSetting.i.WS_GIS_BILLS_TMT_CONN);
    }

    @Override
    public void setWsGisBillsConnTimeout (int i) {
        setInt (VocSetting.i.WS_GIS_BILLS_TMT_CONN, i);
    }

    @Override
    public int getWsGisBillsRespTimeout () {
        return getInt (VocSetting.i.WS_GIS_BILLS_TMT_RESP);
    }

    @Override
    public void setWsGisBillsRespTimeout (int i) {
        setInt (VocSetting.i.WS_GIS_BILLS_TMT_RESP, i);
    }


    @Override
    public String getWsGisHouseManagementUrl() {
       return get (VocSetting.i.WS_GIS_HOUSE_MANAGEMENT_URL);
    }

    @Override
    public void setWsGisHouseManagementUrl(String s) {
        set (VocSetting.i.WS_GIS_HOUSE_MANAGEMENT_URL, s);
        set (VocSetting.i.WS_GIS_URL_ROOT, "");
    }

    @Override
    public int getWsGisHouseManagementConnTimeout() {
        return getInt (VocSetting.i.WS_GIS_HOUSE_MANAGEMENT_TMT_CONN);
    }

    @Override
    public void setWsGisHouseManagementConnTimeout(int i) {
        setInt (VocSetting.i.WS_GIS_HOUSE_MANAGEMENT_TMT_CONN, i);
    }

    @Override
    public int getWsGisHouseManagementRespTimeout() {
        return getInt (VocSetting.i.WS_GIS_HOUSE_MANAGEMENT_TMT_RESP);
    }

    @Override
    public void setWsGisHouseManagementRespTimeout(int i) {
        setInt (VocSetting.i.WS_GIS_HOUSE_MANAGEMENT_TMT_RESP, i);
    }
        
    @Override
    public String getWsGisFilesUrl () {
        return get (VocSetting.i.WS_GIS_FILES_URL);
    }

    @Override
    public void setWsGisFilesUrl (String s) {
        set (VocSetting.i.WS_GIS_FILES_URL, s);
        set (VocSetting.i.WS_GIS_URL_ROOT, "");
    }

    @Override
    public int getWsGisFilesConnTimeout () {
        return getInt (VocSetting.i.WS_GIS_FILES_TMT_CONN);
    }

    @Override
    public void setWsGisFilesConnTimeout (int i) {
        setInt (VocSetting.i.WS_GIS_FILES_TMT_CONN, i);
    }

    @Override
    public int getWsGisFilesRespTimeout () {
        return getInt (VocSetting.i.WS_GIS_FILES_TMT_RESP);
    }

    @Override
    public void setWsGisFilesRespTimeout (int i) {
        setInt (VocSetting.i.WS_GIS_FILES_TMT_RESP, i);
    }
    
    @Override
    public String getGisIdOrganization () {
        return get (VocSetting.i.GIS_ID_ORGANIZATION);
    }

    @Override
    public void setGisIdOrganization (String s) {
        UUID.fromString(s);
        set (VocSetting.i.GIS_ID_ORGANIZATION, s);
    }
    
    @Override
    public String getWsGisUrlRoot()
    {
        return get (VocSetting.i.WS_GIS_URL_ROOT);
    }
    
    @Override
    public void setWsGisUrlRoot(String s)
    {   
        set (VocSetting.i.WS_GIS_URL_ROOT, s);
        for (VocSetting.i i: VocSetting.i.values ()) if (i.isGisEndpointUrl ()) set (i, s + i.toGisEndpointPath ());
    }
    
    @Override
    public String getWsGisBasicLogin () {
        return get (VocSetting.i.WS_GIS_BASIC_LOGIN);
    }

    @Override
    public void setWsGisBasicLogin (String s) {
        set (VocSetting.i.WS_GIS_BASIC_LOGIN, s);
    }

    @Override
    public String getWsGisBasicPassword () {
        return "******";
    }

    @Override
    public void setWsGisBasicPassword (String s) {
        set (VocSetting.i.WS_GIS_BASIC_PASSWORD, s);
    }
    
    
    @Override
    public int getTtlContracts () {
        return getInt (VocSetting.i.TTL_CONTRACTS);
    }

    @Override
    public void setTtlContracts (int i) {
        setInt (VocSetting.i.TTL_CONTRACTS, i);
    }
    
    @Override
    public int getTtlCharters () {
        return getInt (VocSetting.i.TTL_CHARTERS);
    }

    @Override
    public void setTtlCharters (int i) {
        setInt (VocSetting.i.TTL_CHARTERS, i);
    }    
        
    @Override
    public String getWsGisServicesUrl() {
       return get (VocSetting.i.WS_GIS_SERVICES_URL);
    }

    @Override
    public void setWsGisServicesUrl(String s) {
        set (VocSetting.i.WS_GIS_SERVICES_URL, s);
        set (VocSetting.i.WS_GIS_URL_ROOT, "");
    }

    @Override
    public int getWsGisServicesConnTimeout() {
        return getInt (VocSetting.i.WS_GIS_SERVICES_TMT_CONN);
    }

    @Override
    public void setWsGisServicesConnTimeout(int i) {
        setInt (VocSetting.i.WS_GIS_SERVICES_TMT_CONN, i);
    }

    @Override
    public int getWsGisServicesRespTimeout() {
        return getInt (VocSetting.i.WS_GIS_SERVICES_TMT_RESP);
    }

    @Override
    public void setWsGisServicesRespTimeout(int i) {
        setInt (VocSetting.i.WS_GIS_SERVICES_TMT_RESP, i);
    }

}