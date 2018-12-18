package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.DATE;
import static ru.eludia.base.model.Type.STRING;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocLicenseStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class License extends EnTable {
    
    public enum c implements ColEnum {
        
        LICENSEGUID                     (Type.UUID,                         null,   "UUID лицензии в системе"),
        LICENSEVERSIONGUID              (Type.UUID,                         null,   "Идентификатор последней известной версии лицензии" ),
        ID_LOG                          (LicenseLog.class,                  null,   "Последнее событие редактирования"), 
        LICENSENUMBER                   (STRING,                    9,              "Номер лицензии"),
        LICENSE_REG_DATE                (DATE,                                      "Дата регистрации лицензии"),   
        LICENSESTATUS                   (VocLicenseStatus.class,            VocLicenseStatus.i.INACTIVE.asDef (),        "Статус лицензии"),
        LICENSINGAUTHORITY              (VocOrganization.class,                                                          "Наименование лицензирующего органа"),
        REGION_FIAS_GUID                (Type.UUID,                         null,   "Адрес осуществления лицензируемого вида деятельности (код по ФИАС)"),
        LICENSEABLE_TYPE_OF_ACTIVITY    (STRING,                    255,            "Лицензируемый вид деятельности с указанием выполняемых работ, оказываемых услуг, составляющих лицензируемый вид деятельности"),
        ADDITIONAL_INFORMATION          (STRING,                    255,    null,   "Дополнительная информация"),
        LICENSEORGANIZATION             (LicenseOrganization.class,                 "Лицензиат"); 
        
        @Override
        public Col getCol () {return col;}
        
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}        
    
    }
    
    public License () {

        super ("tb_licences", "Лицензии");
        
        cols   (c.class);
        
        key    ("licenseguid", c.LICENSEGUID);
    
    }    
    
}
