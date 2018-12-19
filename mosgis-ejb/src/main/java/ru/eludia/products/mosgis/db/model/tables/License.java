package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.voc.VocLicenseStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import static ru.eludia.base.model.Type.DATE;
import static ru.eludia.base.model.Type.STRING;
import static ru.eludia.base.model.Type.UUID;
import static ru.eludia.base.model.Type.INTEGER;

public class License extends Table {

    public enum c implements ColEnum {
        
        LICENSEGUID                     (UUID,                    null,   "UUID лицензии в системе"),
        LICENSE_VERSION                 (INTEGER,                 null,   "Версия лицензии" ),
        ID_LOG                          (LicenseLog.class,        null,   "Последнее событие редактирования"), 
        LICENSE_NUMBER                  (STRING,            9,            "Номер лицензии"),
        LICENSE_REG_DATE                (DATE,                            "Дата регистрации лицензии"),   
        ID_STATUS                       (VocLicenseStatus.class,  null,   "Статус лицензии с точки зрения mosgis"),
        UUID_ORG_AUTHORITY              (VocOrganization.class,           "Наименование лицензирующего органа"),
        REGION_FIAS_GUID                (UUID,                    null,   "Адрес осуществления лицензируемого вида деятельности (код по ФИАС)"),
        LICENSEABLE_TYPE_OF_ACTIVITY    (STRING,            2000,         "Лицензируемый вид деятельности с указанием выполняемых работ, оказываемых услуг, составляющих лицензируемый вид деятельности"),
        ADDITIONAL_INFORMATION          (STRING,            2000, null,   "Дополнительная информация"),
        UUID_ORG                        (VocOrganization.class,           "Лицензиат"); 
        
        @Override
        public Col getCol () {return col;}
        
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}        
    
    }
    
    public License () {

        super ("tb_licences", "Лицензии");
        
        cols  (c.class);
        
        pk    (c.LICENSEGUID);
        key   ("uuid_org", c.UUID_ORG);
    
    }    
    
}
