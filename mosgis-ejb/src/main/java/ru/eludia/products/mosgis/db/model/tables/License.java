package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.voc.VocLicenseStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import static ru.eludia.base.model.Type.DATE;
import static ru.eludia.base.model.Type.STRING;
import static ru.eludia.base.model.Type.INTEGER;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;

public class License extends Table {

    public enum c implements EnColEnum {
        
        LICENSEGUID                     (Type.UUID,                       "UUID лицензии в системе"),
        LICENSE_VERSION                 (INTEGER,                 null,   "Версия лицензии" ),
        LICENSE_NUMBER                  (STRING,            9,            "Номер лицензии"),
        LICENSE_REG_DATE                (DATE,                            "Дата регистрации лицензии"),   
        ID_STATUS                       (VocLicenseStatus.class,  null,   "Статус лицензии"),
        UUID_ORG_AUTHORITY              (VocOrganization.class,           "Наименование лицензирующего органа"),
        REGION_FIAS_GUID                (VocBuilding.class,       null,   "Адрес осуществления лицензируемого вида деятельности (код по ФИАС)"),
        LICENSEABLE_TYPE_OF_ACTIVITY    (STRING,            2000,         "Лицензируемый вид деятельности с указанием выполняемых работ, оказываемых услуг, составляющих лицензируемый вид деятельности"),
        ADDITIONAL_INFORMATION          (STRING,            2000, null,   "Дополнительная информация"),
        UUID_ORG                        (VocOrganization.class,           "Лицензиат"), 
        
        ID_LOG                          (LicenseLog.class,        null,   "Последнее запрос"),
        
        UUID                            (Type.UUID, new Virt("HEXTORAW(''||RAWTOHEX(\"LICENSEGUID\"))"),                                      "uuid"),
        LABEL                           (STRING,    new Virt("'№' || license_number || ' от ' || TO_CHAR (license_reg_date, 'DD.MM.YYYY')"), "№/дата");
        
        @Override
        public Col getCol() {
            return col;
        }
        private Col col;

        private c(Type type, Object... p) {
            col = new Col(this, type, p);
        }

        private c(Class c, Object... p) {
            col = new Ref(this, c, p);
        }
        
        @Override
        public boolean isLoggable() {
            switch (this) {
                case LICENSEGUID:
                case UUID:
                case LABEL:
                case ID_LOG:
                    return false;
                default:
                    return true;
            }
        }
    
    }
    
    public License() {

        super("tb_licenses", "Лицензии");

        cols(c.class);

        pk(c.LICENSEGUID);
        key("uuid_org", c.UUID_ORG);

    }
    
}
