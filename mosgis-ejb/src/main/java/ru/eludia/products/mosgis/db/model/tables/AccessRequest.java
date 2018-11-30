package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.voc.VocAccessRequestStatus;
import ru.eludia.products.mosgis.db.model.voc.VocAccessRequestType;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class AccessRequest extends Table {

    public enum c implements ColEnum {
        
        ACCESSREQUESTGUID (Type.UUID, "ИД заявки"),
        ORGROOTENTITYGUID (VocOrganization.class, "Организация в реестре организаций"),
        TYPE_             (VocAccessRequestType.class, "Тип заявки"),
        STATUS            (VocAccessRequestStatus.class, "Статус заявки"),

        APPLICATIONDATE   (Type.DATE, "Дата подачи"),
        STARTDATE         (Type.DATE, "Дата начала"),
        ENDDATE           (Type.DATE, null, "Дата окончания"),
        
        STATUSCHANGEDATE  (Type.DATE, "Дата статуса"),
        STATUSREASON      (Type.STRING, "Причина статуса"),
                        
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}


    }

    public AccessRequest () {        
        super  ("tb_acc_req", "Заявки на предоставление доступа");
        cols   (c.class);
        pk     (c.ACCESSREQUESTGUID);
        key    ("uuid_contract", c.ORGROOTENTITYGUID);        
    }
    
//    private static final Logger logger = Logger.getLogger (AccessRequest.class.getName ());    
    
}