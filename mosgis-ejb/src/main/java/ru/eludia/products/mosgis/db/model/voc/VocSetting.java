package ru.eludia.products.mosgis.db.model.voc;

import javax.xml.ws.BindingProvider;
import ru.eludia.base.DB;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.jmx.Conf;

public class VocSetting extends Table {

    public VocSetting () {

        super ("vc_settings", "Параметры настройки системы");

        pk    ("id",    Type.STRING,       "Ключ");        
        col   ("label", Type.STRING,       "Наименование");
        col   ("value", Type.STRING, null, "Значение по умолчанию");

        data  (i.class);
        
    }
    
    public static void setPort (Object p, String url, int connectTimeout, int responseTimeout) {

        java.util.Map <String,Object> c = ((BindingProvider) p).getRequestContext ();

        c.put (BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
        
        final String basicLogin = Conf.get (i.WS_GIS_BASIC_LOGIN);
        if (DB.ok (basicLogin)) {
            c.put (BindingProvider.USERNAME_PROPERTY, basicLogin);
            c.put (BindingProvider.PASSWORD_PROPERTY, Conf.get (i.WS_GIS_BASIC_PASSWORD));
        }        
        
        c.put (com.sun.xml.ws.developer.JAXWSProperties.CONNECT_TIMEOUT, connectTimeout);
        c.put ("com.sun.xml.ws.request.timeout", responseTimeout);
                      
    }
    
    public static void setPort (Object port, String prefix) {

        setPort (port, 
            Conf.get    (i.valueOf (prefix + "_URL")), 
            Conf.getInt (i.valueOf (prefix + "_TMT_CONN")), 
            Conf.getInt (i.valueOf (prefix + "_TMT_RESP")) 
        );

    }
    
    public static final String WS_GIS_URL_ROOT_DEFAULT = "https://217.107.108.156:10081";
    
    public enum i {
        
        USER_ADMIN_LOGIN ("user.admin.login", "УЗ администратора для первого входа: login", "test"),
        USER_ADMIN_PASSWORD ("user.admin.password", "УЗ администратора для первого входа: пароль", "test"),
                
        PATH_OKEI ("path.okei", "URL JSON/ZIP ОКЕИ на портале открытых данных Правительства Москвы", "c:/projects/mosgis/incoming/opendata/okei"),
        PATH_OKTMO ("path.oktmo", "Директория с CSV-файлом, содержищим справочник ОКТМО", "c:/projects/mosgis/incoming/opendata/oktmo"),
        PATH_OKSM("path.oksm", "URL JSON/ZIP ОКСМ на портале открытых данных Правительства Москвы", "c:/projects/mosgis/incoming/opendata/oksm"),
        PATH_FIAS ("path.fias", "Директория с распакованной из RAR XML-выгрузкой ФИАС", "c:/projects/mosgis/incoming/fias/fias_xml"),
        PATH_UNOM ("path.unom", "Директория с CSV-файлом, содержищим мапинг UNOM", "c:/projects/mosgis/incoming/unom"),
        PATH_OPENDATA ("path.opendata", "Директория с распакованной из ZIP XML-выгрузкой с портала открытых данных Правительства Москвы", "c:/projects/mosgis/incoming/opendata"),

        WS_GIS_BASIC_LOGIN    ("ws.gis.basic.login", "Login Basic-аутентификации сервисов GIS", ""),
        WS_GIS_BASIC_PASSWORD ("ws.gis.basic.password", "Пароль Basic-аутентификации сервисов GIS", ""),
        WS_GIS_URL_ROOT       ("ws.gis.url.root", "Корневой URL сервисов GIS", ""),
        WS_GIS_ASYNC_TTL      ("ws.gis.async.ttl", "Время жизни асинхронных операций в ГИС, мин.", "1"),
        
        WS_GIS_FILES_URL      ("ws.gis.files.url", "Endpoint URL файлового REST-сервиса ГИС ЖКХ", WS_GIS_URL_ROOT_DEFAULT + "/ext-bus-file-store-service/rest/"),
        WS_GIS_FILES_TMT_CONN ("ws.gis.files.timeout.connection", "Время ожидания подключения к файловому REST-сервису ГИС ЖКХ, мс", "10000"),
        WS_GIS_FILES_TMT_RESP ("ws.gis.files.timeout.response", "Время ожидания подключения к файловому REST-сервису ГИС ЖКХ (BillsServiceAsync), мс", "10000"),

        WS_GIS_BILLS_URL      ("ws.gis.bills.url", "Endpoint URL сервиса обмена сведениями о начислениях, взаиморасчетах ГИС ЖКХ (BillsServiceAsync)", WS_GIS_URL_ROOT_DEFAULT + "/ext-bus-bills-service/services/BillsAsync"),
        WS_GIS_BILLS_TMT_CONN ("ws.gis.bills.timeout.connection", "Время ожидания подключения к сервису обмена сведениями о начислениях, взаиморасчетах ГИС ЖКХ (BillsServiceAsync), мс", "10000"),
        WS_GIS_BILLS_TMT_RESP ("ws.gis.bills.timeout.response", "Время ожидания подключения к сервису обмена сведениями о начислениях, взаиморасчетах ГИС ЖКХ (BillsServiceAsync), мс", "10000"),

        WS_GIS_LICENSES_URL      ("ws.gis.licenses.url", "Endpoint URL сервиса управления экспортом лицензий и дисквалифицированных лиц ГИС ЖКХ (LicenseServiceAsync)", WS_GIS_URL_ROOT_DEFAULT + "/ext-bus-licenses-service/services/LicensesAsync"),
        WS_GIS_LICENSES_TMT_CONN ("ws.gis.licenses.timeout.connection", "Время ожидания подключения к сервису управления экспортом лицензий и дисквалифицированных лиц ГИС ЖКХ (LicenseServiceAsync), мс", "10000"),
        WS_GIS_LICENSES_TMT_RESP ("ws.gis.licenses.timeout.response", "Время ожидания подключения к сервису управления экспортом лицензий и дисквалифицированных лиц ГИС ЖКХ (LicenseServiceAsync), мс", "10000"),
        
        WS_GIS_NSI_URL      ("ws.gis.nsi.url", "Endpoint URL сервиса частной НСИ ГИС ЖКХ (NsiAsyncService)", WS_GIS_URL_ROOT_DEFAULT + "/ext-bus-nsi-service/services/NsiAsync"),
        WS_GIS_NSI_TMT_CONN ("ws.gis.nsi.timeout.connection", "Время ожидания подключения к сервису частной НСИ ГИС ЖКХ (NsiAsyncService), мс", "10000"),
        WS_GIS_NSI_TMT_RESP ("ws.gis.nsi.timeout.response", "Время ожидания подключения к сервису частной НСИ ГИС ЖКХ (NsiAsyncService), мс", "10000"),

        WS_GIS_NSI_COMMON_URL      ("ws.gis.nsi.common.url", "Endpoint URL сервиса НСИ ГИС ЖКХ (NsiCommonAsyncService)", WS_GIS_URL_ROOT_DEFAULT + "/ext-bus-nsi-common-service/services/NsiCommonAsync"),
        WS_GIS_NSI_COMMON_TMT_CONN ("ws.gis.nsi.common.timeout.connection", "Время ожидания подключения к сервису НСИ ГИС ЖКХ (NsiCommonAsyncService), мс", "10000"),
        WS_GIS_NSI_COMMON_TMT_RESP ("ws.gis.nsi.common.timeout.response", "Время ожидания подключения к сервису НСИ ГИС ЖКХ (NsiCommonAsyncService), мс", "10000"),

        WS_GIS_ORG_COMMON_URL      ("ws.gis.org.common.url", "Endpoint URL сервиса реестра юрлиц ГИС ЖКХ (OrgRegistryCommonAsyncService)", WS_GIS_URL_ROOT_DEFAULT + "/ext-bus-org-registry-common-service/services/OrgRegistryCommonAsync"),
        WS_GIS_ORG_COMMON_TMT_CONN ("ws.gis.org.common.timeout.connection", "Время ожидания подключения к сервису реестра юрлиц ГИС ЖКХ (OrgRegistryCommonAsyncService), мс", "10000"),
        WS_GIS_ORG_COMMON_TMT_RESP ("ws.gis.org.common.timeout.response", "Время ожидания подключения к сервису реестра юрлиц ГИС ЖКХ (OrgRegistryCommonAsyncService), мс", "10000"),

        WS_GIS_ORG_URL      ("ws.gis.org.url", "Endpoint URL сервиса импорта подразделений юрлиц ГИС ЖКХ (OrgRegistryAsync)", WS_GIS_URL_ROOT_DEFAULT + "/ext-bus-org-registry-service/services/OrgRegistryAsync"),
        WS_GIS_ORG_TMT_CONN ("ws.gis.org.timeout.connection", "Время ожидания подключения к сервису импорта подразделений юрлиц ГИС ЖКХ (OrgRegistryAsync), мс", "10000"),
        WS_GIS_ORG_TMT_RESP ("ws.gis.org.timeout.response", "Время ожидания подключения к сервису импорта подразделений юрлиц ГИС ЖКХ (OrgRegistryAsync), мс", "10000"),
        
        WS_GIS_HOUSE_MANAGEMENT_URL      ("ws.gis.house.management.url", "Endpoint URL сервиса обмена сведениями о жилищном фонде ГИС ЖКХ (HouseManagementServiceAsync)", WS_GIS_URL_ROOT_DEFAULT + "/ext-bus-home-management-service/services/HomeManagementAsync"),
        WS_GIS_HOUSE_MANAGEMENT_TMT_CONN ("ws.gis.house.management.timeout.connection", "Время ожидания подключения к сервису обмена сведениями о жилищном фонде ГИС ЖКХ (HouseManagementServiceAsync), мс", "10000"),
        WS_GIS_HOUSE_MANAGEMENT_TMT_RESP ("ws.gis.house.management.timeout.response", "Время ожидания подключения к сервису обмена сведениями о жилищном фонде ГИС ЖКХ (HouseManagementServiceAsync), мс", "10000"),
        
        WS_GIS_SERVICES_URL      ("ws.gis.services.url", "Endpoint URL сервиса обмена сведениями об услугах ГИС ЖКХ (ServicesServiceAsync)", WS_GIS_URL_ROOT_DEFAULT + "/ext-bus-organization-service/services/OrganizationAsync"),
        WS_GIS_SERVICES_TMT_CONN ("ws.gis.services.timeout.connection", "Время ожидания подключения к сервису обмена сведениями об услугах ГИС ЖКХ (ServicesServiceAsync), мс", "10000"),
        WS_GIS_SERVICES_TMT_RESP ("ws.gis.services.timeout.response", "Время ожидания подключения к сервису обмена сведениями об услугах ГИС ЖКХ (ServicesServiceAsync), мс", "10000"),

        WS_RD_URL      ("ws.rd.url", "Endpoint URL сервиса ГИС РД (WS)", "http://37.230.149.85:8733/DMWS/"),
        WS_RD_TMT_CONN ("ws.rd.timeout.connection", "Время ожидания подключения к сервису ГИС РД (NsiCommonAsyncService), мс", "1000"),
        WS_RD_TMT_RESP ("ws.rd.timeout.response", "Время ожидания подключения к сервису ГИС РД (NsiCommonAsyncService), мс", "1000"),
        
        GIS_ID_ORGANIZATION ("gis.id.organization", "ГИС-идентификатор организации-отправителя данных", "392cf4d1-3527-4b99-9f60-7c3ecbba11d8");

        String id;
        String label;
        String value;
            
        public boolean isGisEndpointUrl () {
            final String name = name ();
            return name.endsWith ("_URL") && name.startsWith ("WS_GIS_");
        }
        
        public String toGisEndpointPath () {
            return value.substring (WS_GIS_URL_ROOT_DEFAULT.length());
        }

        public String getId () {
            return id;
        }

        public String getValue () {
            return value;
        }
        
        public String getLabel () {
            return label;
        }

        private i (String id, String label, String value) {
            this.id = id;
            this.value = value;
            this.label = label;
        }
        
    }
    
}