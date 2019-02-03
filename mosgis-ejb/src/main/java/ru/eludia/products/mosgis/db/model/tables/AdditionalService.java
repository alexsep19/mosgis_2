package ru.eludia.products.mosgis.db.model.tables;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.db.util.SyncMap;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Num;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState;
import static ru.eludia.products.mosgis.db.model.voc.VocAsyncEntityState.i.PENDING;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementFieldType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementOkeiRefFieldType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementStringFieldType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiRef;

public class AdditionalService extends Table {

    public AdditionalService () {

        super ("tb_add_services",                                                       "Дополнительные услуги");

        pk    ("uuid",                      Type.UUID,             NEW_UUID,            "Ключ");
        fk    ("uuid_org",                  VocOrganization.class,                      "Организация");
        col   ("is_deleted",                Type.BOOLEAN,          Bool.FALSE,          "1, если запись удалена; иначе 0");
                
        fk    ("okei",                      VocOkei.class,                      null,   "Единица измерения");
        col   ("additionalservicetypename", Type.STRING,           100,         null,   "Наименование вида дополнительной услуги");

        col   ("uniquenumber",              Type.STRING,                        null,   "Уникальный реестровый номер (в ГИС)");
        col   ("elementguid",               Type.UUID,                          null,   "Идентификатор существующей в ГИС версии элемента справочника");

        col   ("label",                     Type.STRING,  new Virt ("(''||\"ADDITIONALSERVICETYPENAME\")"),  "Наименование");
        col   ("label_uc",                  Type.STRING,  new Virt ("UPPER(\"ADDITIONALSERVICETYPENAME\")"),  "НАИМЕНОВАНИЕ В ВЕРХНЕМ РЕГИСТРЕ");

        fk    ("id_status",                 VocAsyncEntityState.class,          new Num (VocAsyncEntityState.i.PENDING.getId ()), "Статус синхронизации");
        fk    ("id_log",                    AdditionalServiceLog.class,         null, "Последнее событие редактирования");

        key   ("label_uc", "label_uc");
        key   ("org_label", "uuid_org", "label");

        trigger ("BEFORE INSERT OR UPDATE", "BEGIN "

            + "IF UPDATING "
            + "  AND :OLD.id_log IS NOT NULL "             // что-то уже отправляли
            + "  AND :OLD.id_log <> :NEW.id_log "          // пытаются отредактировать вновь
            + "  AND :OLD.id_status = " + PENDING.getId () // прошлая синхронизация не доехала
            + " THEN"
            + "  raise_application_error (-20000, 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.'); "
            + "END IF; "

        + "END;");        

    }
    
    private final static String [] keyFields = {"uniquenumber"};

    public class Sync extends SyncMap<NsiRef> {
        
        UUID uuid_org;

        public Sync (DB db, UUID uuid_org) {
            super (db);
            this.uuid_org = uuid_org;
            commonPart.put ("uuid_org", uuid_org);
            commonPart.put ("is_deleted", 0);
        }                

        @Override
        public String[] getKeyFields () {
            return keyFields;
        }

        @Override
        public void setFields (Map<String, Object> h, NsiRef o) {
            h.put ("uniquenumber", o.getCode ());
        }

        @Override
        public Table getTable () {
            return AdditionalService.this;
        }
        
    }
    
    public static Map<String, Object> toHASH (NsiElementType t) {
        
        final Map<String, Object> result = DB.HASH (
            "is_deleted",   t.isIsActual () ? 0 : 1,
            "uniquenumber", t.getCode (),
            "elementguid",  t.getGUID ()
        );

        for (NsiElementFieldType f: t.getNsiElementField ()) {
            
            if (f instanceof NsiElementOkeiRefFieldType) {
                result.put ("okei", ((NsiElementOkeiRefFieldType) f).getCode ());
            }
            else if (f instanceof NsiElementStringFieldType && "Вид дополнительной услуги".equals (f.getName ())) {
                result.put ("additionalservicetypename", ((NsiElementStringFieldType) f).getValue ());
            }
            
        }

        return result;
        
    }    

}