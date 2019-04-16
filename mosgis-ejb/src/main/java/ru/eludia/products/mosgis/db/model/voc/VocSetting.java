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

        WS_GIS_BASIC_LOGIN    ("ws.gis.basic.login", "Login Basic-аутентификации сервисов GIS", ""),
        WS_GIS_BASIC_PASSWORD ("ws.gis.basic.password", "Пароль Basic-аутентификации сервисов GIS", ""),
        WS_GIS_URL_ROOT       ("ws.gis.url.root", "Корневой URL сервисов GIS", ""),
        WS_GIS_ASYNC_TTL      ("ws.gis.async.ttl", "Время жизни асинхронных операций в ГИС, мин.", "30"),
        
        WS_GIS_FILES_URL      ("ws.gis.files.url", "Endpoint URL файлового REST-сервиса ГИС ЖКХ", WS_GIS_URL_ROOT_DEFAULT + "/ext-bus-file-store-service/rest/"),
        WS_GIS_FILES_TMT_CONN ("ws.gis.files.timeout.connection", "Время ожидания подключения к файловому REST-сервису ГИС ЖКХ, мс", "10000"),
        WS_GIS_FILES_TMT_RESP ("ws.gis.files.timeout.response", "Время ожидания подключения к файловому REST-сервису ГИС ЖКХ (BillsServiceAsync), мс", "10000"),

        WS_GIS_BILLS_URL      ("ws.gis.bills.url", "Endpoint URL сервиса обмена сведениями о начислениях, взаиморасчетах ГИС ЖКХ (BillsServiceAsync)", WS_GIS_URL_ROOT_DEFAULT + "/ext-bus-bills-service/services/BillsAsync"),
        WS_GIS_BILLS_TMT_CONN ("ws.gis.bills.timeout.connection", "Время ожидания подключения к сервису обмена сведениями о начислениях, взаиморасчетах ГИС ЖКХ (BillsServiceAsync), мс", "10000"),
        WS_GIS_BILLS_TMT_RESP ("ws.gis.bills.timeout.response", "Время ожидания подключения к сервису обмена сведениями о начислениях, взаиморасчетах ГИС ЖКХ (BillsServiceAsync), мс", "10000"),
        
        WS_GIS_CAPITAL_REPAIR_URL      ("ws.gis.capital.repair.url", "Endpoint URL сервиса обмена сведениями о капитальных ремонтах ГИС ЖКХ (CapitalRepairServiceAsync)", WS_GIS_URL_ROOT_DEFAULT + "/ext-bus-capital-repair-programs-service/services/CapitalRepairAsync"),
        WS_GIS_CAPITAL_REPAIR_TMT_CONN ("ws.gis.capital.repair.timeout.connection", "Время ожидания подключения к сервису обмена сведениями о капитальных ремонтах ГИС ЖКХ, мс", "10000"),
        WS_GIS_CAPITAL_REPAIR_TMT_RESP ("ws.gis.capital.repair.timeout.response", "Время ожидания подключения к сервису обмена сведениями о капитальных ремонтах ГИС ЖКХ, мс", "10000"),
        
        WS_GIS_INFRASTRUCTURES_URL      ("ws.gis.infrastructures.url", "Endpoint URL сервиса обмена сведениями об объектах коммунальной инфраструктуры ГИС ЖКХ (InfrastructureServiceAsync)", WS_GIS_URL_ROOT_DEFAULT + "/ext-bus-rki-service/services/InfrastructureAsync"),
        WS_GIS_INFRASTRUCTURES_TMT_CONN ("ws.gis.infrastructures.timeout.connection", "Время ожидания подключения к сервису обмена сведениями об объектах коммунальной инфраструктуры ГИС ЖКХ, мс", "10000"),
        WS_GIS_INFRASTRUCTURES_TMT_RESP ("ws.gis.infrastructures.timeout.response", "Время ожидания подключения к сервису обмена сведениями об объектах коммунальной инфраструктуры ГИС ЖКХ, мс", "10000"),

        WS_GIS_INSPECTION_URL      ("ws.gis.inspection.url", "Endpoint URL сервиса работы с планами проверок и проверками ГИС ЖКХ (InspectionServiceAsync)", WS_GIS_URL_ROOT_DEFAULT + "/ext-bus-inspection-service/services/InspectionAsync"),
        WS_GIS_INSPECTION_TMT_CONN ("ws.gis.inspection.timeout.connection", "Время ожидания подключения к сервису работы с планами проверок и проверками ГИС ЖКХ, мс", "10000"),
        WS_GIS_INSPECTION_TMT_RESP ("ws.gis.inspection.timeout.response", "Время ожидания подключения к сервису работы с планами проверок и проверками ГИС ЖКХ, мс", "10000"),
        
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

        WS_GIS_METERING_URL      ("ws.gis.metering.url", "Endpoint URL сервиса обмена сведениями об услугах ГИС ЖКХ (DeviceMetering)", WS_GIS_URL_ROOT_DEFAULT + "/ext-bus-device-metering-service/services/DeviceMeteringAsync"),
        WS_GIS_METERING_TMT_CONN ("ws.gis.metering.timeout.connection", "Время ожидания подключения к сервису обмена сведениями об услугах ГИС ЖКХ (DeviceMetering), мс", "10000"),
        WS_GIS_METERING_TMT_RESP ("ws.gis.metering.timeout.response", "Время ожидания подключения к сервису обмена сведениями об услугах ГИС ЖКХ (DeviceMetering), мс", "10000"),

        WS_GIS_VOLUME_QUALITY_URL("ws.gis.volume.quality.url", "Endpoint URL сервиса обмена сведениями об объеме и качестве коммунальных услуг и коммунальных ресурсов ГИС ЖКХ (ServicesServiceAsync)", WS_GIS_URL_ROOT_DEFAULT + "/ext-bus-volume-quality-service/services/VolumeQualityAsync/"),
	WS_GIS_VOLUME_QUALITY_TMT_CONN("ws.gis.volume.quality.timeout.connection", "Время ожидания подключения к сервису обмена сведениями об объеме и качестве коммунальных услуг и коммунальных ресурсов  ГИС ЖКХ (VolumeQualityServiceAsync), мс", "10000"),
	WS_GIS_VOLUME_QUALITY_TMT_RESP("ws.gis.volume.quality.timeout.response", "Время ожидания подключения к сервису обмена сведениями об об объеме и качестве коммунальных услуг и коммунальных ресурсов ГИС ЖКХ (VolumeQualityServiceAsync), мс", "10000"),

        WS_RD_URL      ("ws.rd.url", "Endpoint URL сервиса ГИС РД (WS)", "http://37.230.149.85:8733/DMWS/"),
        WS_RD_TMT_CONN ("ws.rd.timeout.connection", "Время ожидания подключения к сервису ГИС РД (NsiCommonAsyncService), мс", "1000"),
        WS_RD_TMT_RESP ("ws.rd.timeout.response", "Время ожидания подключения к сервису ГИС РД (NsiCommonAsyncService), мс", "1000"),

        WS_GIS_TARIFF_URL ("ws.gis.tariff.url", "Endpoint URL сервиса обмена сведениями о тарифах ГИС ЖКХ (TariffAsyncService)", WS_GIS_URL_ROOT_DEFAULT + "/ext-bus-tariff-service/services/TariffAsync/"),
	WS_GIS_TARIFF_TMT_CONN ("ws.gis.tariff.timeout.connection", "Время ожидания подключения к сервису обмена сведениями о тарифах ГИС ЖКХ (TariffAsyncService), мс", "10000"),
	WS_GIS_TARIFF_TMT_RESP ("ws.gis.tariff.timeout.response", "Время ожидания подключения к сервису обмена сведениями об о тарифах ГИС ЖКХ (TariffAsyncService), мс", "10000"),

	WS_GIS_UK_URL("ws.gis.uk.url", "Endpoint URL сервиса обмена сведениями о нормативно-правовых документах ГИС ЖКХ (UkAsyncService)", WS_GIS_URL_ROOT_DEFAULT + "/ext-bus-uk-service/services/UkAsyncService/"),
	WS_GIS_UK_TMT_CONN("ws.gis.uk.timeout.connection", "Время ожидания подключения к сервису обмена сведениями о нормативно-правовых документах ГИС ЖКХ (UkAsyncService), мс", "10000"),
	WS_GIS_UK_TMT_RESP("ws.gis.uk.timeout.response", "Время ожидания подключения к сервису обмена сведениями об о нормативно-правовых документах ГИС ЖКХ (UkAsyncService), мс", "10000"),

	WS_GIS_PAYMENT_URL("ws.gis.payment.url", "Endpoint URL сервиса обмена сведениями о платежах и ПД ГИС ЖКХ (PaymentAsync)", WS_GIS_URL_ROOT_DEFAULT + "/ext-bus-payment-service/services/PaymentAsync/"),
	WS_GIS_PAYMENT_TMT_CONN("ws.gis.payment.timeout.connection", "Время ожидания подключения к сервису обмена сведениями платежах и ПД ГИС ЖКХ (PaymentAsync), мс", "10000"),
	WS_GIS_PAYMENT_TMT_RESP("ws.gis.payment.timeout.response", "Время ожидания подключения к сервису обмена сведениями об о платежах и ПД ГИС ЖКХ (PaymentAsync), мс", "10000"),

	WS_GIS_MSP_URL("ws.gis.msp.url", "Endpoint URL сервиса обмена сведениями о мерах соцподдержки ГИС ЖКХ (PaymentAsync)", WS_GIS_URL_ROOT_DEFAULT + "/ext-bus-msp-service/services/MSPAsync/"),
	WS_GIS_MSP_TMT_CONN("ws.gis.msp.timeout.connection", "Время ожидания подключения к сервису обмена сведениями о мерах соцподдержки ГИС ЖКХ (MSPAsync), мс", "10000"),
	WS_GIS_MSP_TMT_RESP("ws.gis.msp.timeout.response", "Время ожидания подключения к сервису обмена сведениями о мерах соцподдержки ГИС ЖКХ (MSPAsync), мс", "10000"),

        GIS_ID_ORGANIZATION ("gis.id.organization", "ГИС-идентификатор организации-отправителя данных", "392cf4d1-3527-4b99-9f60-7c3ecbba11d8"),
        
        URL_BIC ("url.bic", "URL zip-архива с XML-документом, содержащим актуальный реестр БИК финансовых организаций РФ", "http://cbr.ru/s/newbik"),
        
        ;

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