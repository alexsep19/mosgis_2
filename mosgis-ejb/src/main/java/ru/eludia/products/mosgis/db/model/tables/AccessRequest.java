package ru.eludia.products.mosgis.db.model.tables;

import java.util.List;
import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.voc.VocAccessRequestStatus;
import ru.eludia.products.mosgis.db.model.voc.VocAccessRequestType;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common.ExportDelegatedAccessType;

public class AccessRequest extends Table {

    public static final String TABLE_NAME = "tb_acc_req";

    public enum c implements ColEnum {
        
        ACCESSREQUESTGUID (Type.UUID, "ИД заявки"),
//        ORGROOTENTITYGUID (VocOrganization.class, "Организация в реестре организаций"),
        ORGROOTENTITYGUID (Type.UUID, "Организация в реестре организаций"),
        TYPE_             (VocAccessRequestType.class, "Тип заявки"),
        STATUS            (VocAccessRequestStatus.class, "Статус заявки"),

        APPLICATIONDATE   (Type.DATE, "Дата подачи"),
        STARTDATE         (Type.DATE, "Дата начала"),
        ENDDATE           (Type.DATE,   null, "Дата окончания"),
        
        STATUSCHANGEDATE  (Type.DATE,   null, "Дата статуса"),
        STATUSREASON      (Type.STRING, null, "Причина статуса"),
                        
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}


    }

    public AccessRequest () {        
        super  (TABLE_NAME, "Заявки на предоставление доступа");
        cols   (c.class);
        pk     (c.ACCESSREQUESTGUID);
        key    ("uuid_contract", c.ORGROOTENTITYGUID);        
    }
    
//    private static final Logger logger = Logger.getLogger (AccessRequest.class.getName ());    

    public static void add (List <Map<String, Object>> l, ExportDelegatedAccessType t) {
                
        for (ru.gosuslugi.dom.schema.integration.organizations_registry_common.AccessRequest i: t.getAccessRequest ()) {
            
            l.add (DB.HASH (
                c.ACCESSREQUESTGUID, i.getAccessRequestGUID (),
                c.APPLICATIONDATE, i.getApplicationDate (),
                c.ENDDATE, i.getEndDate (),
                c.ORGROOTENTITYGUID, t.getRegOrg ().getOrgRootEntityGUID (),
                c.STARTDATE, i.getStartDate (),
                c.STATUS, VocAccessRequestStatus.i.valueOf (i.getStatus ()),
                c.STATUSCHANGEDATE, i.getStatusChangeDate (),
                c.STATUSREASON, i.getStatusReason (),
                c.TYPE_, VocAccessRequestType.i.valueOf (i.getType ())
            ));
            
        }                
                
    }            
    
}