package ru.eludia.products.mosgis.db.model.tables;

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

public class MainMunicipalService extends Table {

    public MainMunicipalService () {
        
        super ("tb_municipal_svc", "Коммунальные услуги");
        
        pk    ("uuid",                     Type.UUID,           NEW_UUID,   "Ключ");
        fk    ("uuid_org",                 VocOrganization.class,                      "Организация");
        col   ("is_deleted",               Type.BOOLEAN,          Bool.FALSE,          "1, если запись удалена; иначе 0");

        col   ("elementguid",              Type.UUID,           null,       "Идентификатор существующего элемента справочника");        
        
        col   ("code_vc_nsi_3",            Type.STRING,  20,    null,       "Ссылка на НСИ \"Вид коммунальной услуги\" (реестровый номер 3)");        
        col   ("is_general",               Type.BOOLEAN,        Bool.FALSE, "1, если услуга предоставляется на общедомовые нужды; иначе 0");
        col   ("selfproduced",             Type.BOOLEAN,        Bool.FALSE, "1, если услуга производится самостоятельно; иначе 0");
        col   ("mainmunicipalservicename", Type.STRING,                     "Наименование главной коммунальной услуги");
        col   ("code_vc_nsi_2",            Type.STRING,  20,    null,       "Ссылка на НСИ \"Вид коммунального ресурса\" (реестровый номер 2)");        
        fk    ("okei",                     VocOkei.class,       null,       "Единица измерения");
        col   ("sortorder",                Type.STRING,  3,     null,       "Порядок сортировки");
        
        col   ("label",                    Type.STRING,  new Virt ("(''||\"MAINMUNICIPALSERVICENAME\")"),  "Наименование");
        col   ("label_uc",                 Type.STRING,  new Virt ("UPPER(\"MAINMUNICIPALSERVICENAME\")"),  "НАИМЕНОВАНИЕ В ВЕРХНЕМ РЕГИСТРЕ");
        
        col   ("municipalserviceref",      Type.STRING,  new Virt ("(''||\"CODE_VC_NSI_3\")"),  "Вид коммунальной услуги");
        col   ("municipalresourceref",     Type.STRING,  new Virt ("(''||\"CODE_VC_NSI_2\")"),  "Вид коммунального ресурса");
        col   ("generalneeds",             Type.STRING,  new Virt ("DECODE(\"IS_GENERAL\",1,1,NULL)"),  "Признак \"Услуга предоставляется на общедомовые нужды\"");

        fk    ("id_status",                 VocAsyncEntityState.class,          new Num (VocAsyncEntityState.i.PENDING.getId ()), "Статус синхронизации");
        fk    ("id_log",                    MainMunicipalServiceLog.class,          null, "Последнее событие редактирования");

        key   ("org_sort", "uuid_org", "sortorder");
        
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
    
}
