package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Num;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocCharterObjectReason;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;

public class CharterObject extends Table {

    public CharterObject () {
        
        super  ("tb_charter_objects", "Объекты уставов");
        
        pk     ("uuid",                    Type.UUID,             NEW_UUID,     "Ключ");
        col    ("is_deleted",              Type.BOOLEAN,          Bool.FALSE,   "1, если запись удалена; иначе 0");
        
        ref    ("uuid_charter",           Charter.class,                      "Ссылка на договор");
//        ref    ("uuid_charter_agreement", CharterFile.class,    null,         "Ссылка на дополнительное соглашение");
        
        fk     ("fiashouseguid",           VocBuilding.class,                   "Глобальный уникальный идентификатор дома по ФИАС");
        
        fk     ("id_reason",               VocCharterObjectReason.class, new Num (VocCharterObjectReason.i.CHARTER.getId ()), "Основание");
        col    ("ismanagedbycontract",     Type.BOOLEAN,          Bool.FALSE,   "Управление многоквартирным домом осуществляется управляющей организацией по договору управления");

        col    ("startdate",               Type.DATE,                           "Дата начала предоставления услуг");
        col    ("enddate",                 Type.DATE,             null,         "Дата окончания предоставления услуг");        
       
        col    ("annulmentinfo",           Type.STRING,           null,       "Причина аннулирования.");
        col    ("is_annuled",              Type.BOOLEAN,          new Virt ("DECODE(\"ANNULMENTINFO\",NULL,0,1)"),  "1, если запись аннулирована; иначе 0");
        
        fk     ("id_ctr_status",           VocGisStatus.class,                  new Num (VocGisStatus.i.PROJECT.getId ()), "Статус объекта договора с точки зрения mosgis");
        fk     ("id_ctr_status_gis",       VocGisStatus.class,                  new Num (VocGisStatus.i.PROJECT.getId ()), "Статус объекта договора с точки зрения ГИС ЖКХ");

        col    ("contractobjectversionguid",     Type.UUID,       null,          "UUID последней версии данного объекта в ГИС ЖКХ");
        
        fk     ("id_log",                  CharterObjectLog.class,  null,      "Последнее событие редактирования");
 
        trigger ("BEFORE INSERT OR UPDATE", ""
                
            + "DECLARE" 
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
            + "BEGIN "
            + " :NEW.id_log := :NEW.id_log; "
/*                
            + "IF INSERTING"
//                + " OR (NVL (:OLD.uuid_charter_agreement, '00') <> NVL (:NEW.uuid_charter_agreement, '00'))"
                + " OR (NVL (:OLD.annulmentinfo, CHR(0)) <> NVL (:NEW.annulmentinfo, CHR(0)))"
                + " OR (:OLD.startdate <> :NEW.startdate)"
                + " OR (:OLD.enddate   <> :NEW.enddate)"
            + " THEN "
            + " FOR i IN (SELECT uuid FROM tb_charters WHERE uuid=:NEW.uuid_charter AND id_ctr_status NOT IN (10, 11) AND charterversionguid IS NOT NULL) LOOP"
            + "   raise_application_error (-20000, 'Внесение изменений в договор в настоящее время запрещено. Операция отменена.'); "
            + " END LOOP; "
            + "END IF; "
                
            + "IF :NEW.is_deleted = 0 THEN "
            + " FOR i IN ("
                + "SELECT "
                + " o.startdate"
                + " , o.enddate"
                + " , c.docnum"
                + " , c.signingdate"
                + " , org.label "
                + "FROM "
                + " tb_charter_objects o "
                + " INNER JOIN tb_charters c ON o.uuid_charter = c.uuid"
                + " INNER JOIN vc_orgs org    ON c.uuid_org      = org.uuid "
                + "WHERE o.is_deleted = 0"
                + " AND o.is_annuled = 0"
                + " AND o.fiashouseguid = :NEW.fiashouseguid "
                + " AND o.enddate   >= :NEW.startdate "
                + " AND o.startdate <= :NEW.enddate "
                + " AND o.uuid <> NVL(:NEW.uuid, '00') "
                + ") LOOP"
            + " raise_application_error (-20000, "
                + "'Этот адрес обслуживается с ' "
                + "|| TO_CHAR (i.startdate, 'DD.MM.YYYY')"
                + "||' по '"
                + "|| TO_CHAR (i.enddate, 'DD.MM.YYYY')"
                + "||' по договору управления от '"
                + "|| TO_CHAR (i.signingdate, 'DD.MM.YYYY')"
                + "||' №'"
                + "|| i.docnum"
                + "||' с '"
                + "|| i.label"
                + "|| '. Операция отменена.'); "
            + " END LOOP; "
            + "END IF; "
                
            + "IF :OLD.is_deleted = 0 AND :NEW.is_deleted = 1 THEN "
            + " UPDATE tb_charter_services SET is_deleted = 1 WHERE uuid_charter_object = :NEW.uuid; "
            + " COMMIT; "
            + "END IF; "

            + "IF UPDATING AND (NVL (:NEW.contractobjectversionguid, '00') <> NVL (:NEW.contractobjectversionguid, '00')) THEN "
            + " UPDATE tb_charter_objects__log SET contractobjectversionguid = :NEW.contractobjectversionguid WHERE uuid = :NEW.id_log; "
            + " COMMIT; "
            + "END IF; "
*/
                    
        + "END;");
    }
/*
    public static void add (ImportCharterRequest.Charter.PlacingCharter pc, Map<String, Object> r) {

        final ImportCharterRequest.Charter.PlacingCharter.CharterObject co = (ImportCharterRequest.Charter.PlacingCharter.CharterObject) DB.to.javaBean (ImportCharterRequest.Charter.PlacingCharter.CharterObject.class, r);
        
        co.setTransportGUID (UUID.randomUUID ().toString ());

        co.setBaseMService (CharterFile.getBaseServiceType (r));
        
        for (Map<String, Object> service: (List<Map<String, Object>>) r.get ("services")) CharterObjectService.add (co, service);

        pc.getCharterObject ().add (co);        

    }
    
    private static Logger logger = java.util.logging.Logger.getLogger (CharterObject.class.getName ());
    
    public static void add (ImportCharterRequest.Charter.EditCharter ec, Map<String, Object> r) {
        
        final ImportCharterRequest.Charter.EditCharter.CharterObject co = (ImportCharterRequest.Charter.EditCharter.CharterObject) DB.to.javaBean (ImportCharterRequest.Charter.EditCharter.CharterObject.class, r);
        
        co.setTransportGUID (UUID.randomUUID ().toString ());
        
logger.info ("r=" + r);
        
        if (r.get ("contractobjectversionguid") == null) {
            
            final ImportCharterRequest.Charter.EditCharter.CharterObject.Add add = (ImportCharterRequest.Charter.EditCharter.CharterObject.Add) DB.to.javaBean (ImportCharterRequest.Charter.EditCharter.CharterObject.Add.class, r);
            
            add.setBaseMService (CharterFile.getBaseServiceType (r));
            
            for (Map<String, Object> service: (List<Map<String, Object>>) r.get ("services")) CharterObjectService.add (add, service);            
            
            co.setAdd (add);
        
        }
        else {
            
            final Object nul = r.get ("annulmentinfo");
            
            if (nul != null && !nul.toString ().isEmpty ()) {

                final ImportCharterRequest.Charter.EditCharter.CharterObject.Annulment annulment = new ImportCharterRequest.Charter.EditCharter.CharterObject.Annulment ();

                annulment.setCharterObjectVersionGUID (r.get ("contractobjectversionguid").toString ());

                co.setAnnulment (annulment);

            }
            else {

                ImportCharterRequest.Charter.EditCharter.CharterObject.Edit ed = (ImportCharterRequest.Charter.EditCharter.CharterObject.Edit) DB.to.javaBean (ImportCharterRequest.Charter.EditCharter.CharterObject.Edit.class, r);

                ed.setBaseMService (CharterFile.getBaseServiceType (r));

                for (Map<String, Object> service: (List<Map<String, Object>>) r.get ("services")) CharterObjectService.add (ed, service);

                co.setEdit (ed);

            }        
            
        }
        
        ec.getCharterObject ().add (co);
        
    }    
    
    public QP updateStatus (UUID uuid_charter, ExportStatusCAChResultType.CharterObject co) {
    
        QP qp = new QP ("UPDATE ");
                    
        qp.append (getName ());
        qp.add (" SET id_ctr_status_gis         = ?", VocGisStatus.i.forName (co.getManagedObjectStatus ().value ()).getId (), getColumn ("id_ctr_status_gis").toPhysical ());
        qp.add (",    contractobjectversionguid = ?", co.getCharterObjectVersionGUID (),                                      getColumn ("contractobjectversionguid").toPhysical ());

        qp.append (" WHERE is_deleted = 0");
        qp.add (" AND uuid_charter   = ?", uuid_charter,          getColumn ("uuid_charter").toPhysical ());
        qp.add (" AND fiashouseguid   = ?", co.getFIASHouseGuid (), getColumn ("fiashouseguid").toPhysical ());
                    
        return qp;
    
    }
*/
}