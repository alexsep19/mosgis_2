package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;

public class AccountIndividualService extends AttachTable {
    
    public enum c implements EnColEnum {

        UUID_ACCOUNT                 (Account.class,                            "Лицевой счёт"),
        
        UUID_ADD_SERVICE             (AdditionalService.class,                  "Дополнительная услуга"),
        BEGINDATE                    (Type.DATE,                                "Дата начала представления услуги"),
        ENDDATE                      (Type.DATE,                                "Дата окончания представления услуги"),

        ID_LOG                       (AccountIndividualServiceLog.class,        "Последнее событие редактирования"),
        
        ID_CTR_STATUS                (VocGisStatus.class, VocGisStatus.DEFAULT, "Статус с точки зрения mosgis"),
        ID_CTR_STATUS_GIS            (VocGisStatus.class, VocGisStatus.DEFAULT, "Статус с точки зрения ГИС ЖКХ"),
        
        ACCOUNTINDIVIDUALSERVICEGUID (Type.UUID,          null,                 "Идентификатор индивидуальной услуги ЛС"),
        
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {

            switch (this) {
                case UUID_ACCOUNT:
                case ID_LOG:
                    return false;
                default:
                    return true;                    
            }

        }

    }    

    public AccountIndividualService () {
        
        super  ("tb_account_svc", "Индивидуальные услуги лицевых счетов");
        
        cols   (c.class);
        
        key    ("parent", c.UUID_ACCOUNT);        
        key    ("attachmentguid", AttachTable.c.ATTACHMENTGUID);
       
        trigger ("BEFORE UPDATE", "BEGIN "
                
//            + " IF :NEW.len IS NULL THEN :NEW.len := -1; END IF; "                
/*
            + "IF (NVL (:OLD.attachmentguid, '00') = NVL (:NEW.attachmentguid, '00')) THEN BEGIN"
            + " UPDATE tb_charter_files__log SET attachmentguid = :NEW.attachmentguid, attachmenthash = :NEW.attachmenthash WHERE uuid = :NEW.id_log; "
            + "END; END IF; "
*/
            + CHECK_LEN

        + "END;");        

    }
    
}