package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Num;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.voc.VocGisContractorType;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class Contract extends Table {

    public Contract () {

        super ("tb_contracts", "Договоры управления");

        pk    ("uuid",                      Type.UUID,             NEW_UUID,            "Ключ");
        col   ("is_deleted",                Type.BOOLEAN,          Bool.FALSE,          "1, если запись удалена; иначе 0");

        fk    ("uuid_org",                  VocOrganization.class,                      "Договородержатель");

        fk    ("uuid_org_contractor",       VocOrganization.class, null,                "Контрагент");
        fk    ("okei",                      VocGisContractorType.class,                 "Тип контрагента");
        
        fk    ("id_status",                 VocGisStatus.class,          new Num (VocGisStatus.i.PROJECT.getId ()), "Статус договора с точки зрения mosgis");
        fk    ("id_status_gis",             VocGisStatus.class,          new Num (VocGisStatus.i.NOT_RUNNING.getId ()), "Статус договора с точки зрения ГИС ЖКХ");
        
        col   ("docnum",                    Type.STRING,           255,         "Номер договора");
        col   ("signingdate",               Type.DATE,                          "Дата заключения");
        col   ("effectivedate",             Type.DATE,                          "Дата вступления в силу");
        col   ("plandatecomptetion",        Type.DATE,                          "Планируемая дата окончания");

        col   ("automaticrolloveroneyear",  Type.BOOLEAN,          Bool.FALSE,  "1, если запись удалена; иначе 0");
        col   ("code_vc_nsi_58",            Type.STRING,           20,   null,  "Ссылка на НСИ \"Основание заключения договора\" (реестровый номер 58)");
 
        col   ("contractbase",              Type.STRING,  new Virt ("(''||\"CODE_VC_NSI_58\")"),  "Основание заключения договора");

/*
        fk    ("id_log",                    AdditionalServiceLog.class,         null, "Последнее событие редактирования");
*/        

        key   ("org_docnum", "uuid_org", "docnum");

/*
        trigger ("BEFORE INSERT OR UPDATE", "BEGIN "

            + "IF UPDATING "
            + "  AND :OLD.id_log IS NOT NULL "             // что-то уже отправляли
            + "  AND :OLD.id_log <> :NEW.id_log "          // пытаются отредактировать вновь
            + "  AND :OLD.id_status = " + PENDING.getId () // прошлая синхронизация не доехала
            + " THEN"
            + "  raise_application_error (-20000, 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.'); "
            + "END IF; "

        + "END;");        
*/
    }

}