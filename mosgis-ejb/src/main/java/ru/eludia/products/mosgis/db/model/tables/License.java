package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.DATE;
import static ru.eludia.base.model.Type.STRING;
import ru.eludia.products.mosgis.db.model.voc.VocLicenseStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class License extends Table {

    public enum c implements ColEnum {
        
        LICENSEGUID                     (Type.UUID,                         null,   "UUID лицензии в системе"),
        LICENSE_VERSION                 (Type.INTEGER,                      null,   "Версия лицензии" ),
        ID_LOG                          (LicenseLog.class,                  null,   "Последнее событие редактирования"), 
        LICENSE_NUMBER                  (STRING,                    9,              "Номер лицензии"),
        LICENSE_REG_DATE                (DATE,                                      "Дата регистрации лицензии"),   
        LICENSE_STATUS                  (VocLicenseStatus.class,            null,   "Статус лицензии с точки зрения mosgis"),
        LICENSING_AUTHORITY             (VocOrganization.class,                     "Наименование лицензирующего органа"),
        REGION_FIAS_GUID                (Type.UUID,                         null,   "Адрес осуществления лицензируемого вида деятельности (код по ФИАС)"),
        LICENSEABLE_TYPE_OF_ACTIVITY    (STRING,                    2000,           "Лицензируемый вид деятельности с указанием выполняемых работ, оказываемых услуг, составляющих лицензируемый вид деятельности"),
        ADDITIONAL_INFORMATION          (STRING,                    2000,    null,  "Дополнительная информация"),
        LICENSE_ORGANIZATION            (VocOrganization.class,                    "Лицензиат"); 
        
        @Override
        public Col getCol () {return col;}
        
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}        
    
    }
    
    public License () {

        super ("tb_licences", "Лицензии");
        
        cols   (c.class);
        
        pk    (c.LICENSEGUID);
        key    ("license_organization", c.LICENSE_ORGANIZATION);
    
    }    
    
}
