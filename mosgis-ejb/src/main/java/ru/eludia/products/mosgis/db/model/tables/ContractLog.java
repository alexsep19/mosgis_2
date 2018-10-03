package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.voc.VocGisCustomerType;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocUser;
public class ContractLog extends Table {

    public ContractLog () {
        
        super ("tb_contracts__log", "История изменения договоров");
        
        pk    ("uuid",                      Type.UUID,             NEW_UUID,            "Ключ");
        col   ("action",                    Type.STRING,                                "Действие");
        fk    ("uuid_object",               Contract.class,                             "Ссылка на запись");
        col   ("ts",                        Type.TIMESTAMP,        NOW,                 "Дата/время события");
        fk    ("uuid_user",                 VocUser.class,                      null,   "Оператор");
        fk    ("uuid_out_soap",             OutSoap.class,                      null,   "Последний запрос на импорт в ГИС ЖКХ");
        col   ("uuid_message",              Type.UUID,                          null,   "UUID запроса в ГИС ЖКХ");
        col   ("contractversionguid",       Type.UUID,                          null,   "Идентификатор версии договора");
        
        col   ("is_deleted",                Type.BOOLEAN,                       null,   "1, если запись удалена; иначе 0");
        fk    ("uuid_org",                  VocOrganization.class,              null,   "Исполнитель");
        fk    ("uuid_org_customer",         VocOrganization.class,              null,   "Заказчик");
        fk    ("id_customer_type",          VocGisCustomerType.class,           null,   "Тип заказчика");        
        fk    ("id_ctr_status",             VocGisStatus.class,                 null,   "Статус договора с точки зрения mosgis");
        fk    ("id_ctr_status_gis",         VocGisStatus.class,                 null,   "Статус договора с точки зрения ГИС ЖКХ");        
        col   ("docnum",                    Type.STRING,           255,         null,   "Номер договора");
        col   ("signingdate",               Type.DATE,                          null,   "Дата заключения");
        col   ("effectivedate",             Type.DATE,                          null,   "Дата вступления в силу");
        col   ("plandatecomptetion",        Type.DATE,                          null,   "Планируемая дата окончания");
        col   ("terminate",                 Type.DATE,                          null,   "Дата расторжения");
        col   ("automaticrolloveroneyear",  Type.BOOLEAN,                       null,   "1, если запись удалена; иначе 0");
        col   ("code_vc_nsi_58",            Type.STRING,           20,          null,   "Ссылка на НСИ \"Основание заключения договора\" (реестровый номер 58)");
        col   ("code_vc_nsi_54",            Type.STRING,           20,          null,  "Ссылка на НСИ \"Основание расторжения договора\" (реестровый номер 54)");
        col   ("ddt_m_start",               Type.NUMERIC,          2,           null,  "Начало периода ввода показаний ПУ (1..31 — конкретное число; 99 — последнее число)");
        col   ("ddt_m_start_nxt",           Type.BOOLEAN,                       null, "1, если начало периода ввода показаний ПУ в следующем месяце; иначе 0");
        col   ("ddt_m_end",                 Type.NUMERIC,          2,           null,  "Окончание периода ввода показаний ПУ (1..31 — конкретное число; 99 — последнее число)");
        col   ("ddt_m_end_nxt",             Type.BOOLEAN,                       null, "1, если окончание периода ввода показаний ПУ в следующем месяце; иначе 0");
        col   ("ddt_d_start",               Type.NUMERIC,          2,           null,  "Срок представления (выставления) платежных документов для внесения платы за жилое помещение и (или) коммунальные услуги (1..30 — конкретное число; 99 — последнее число)");
        col   ("ddt_d_start_nxt",           Type.BOOLEAN,                       null, "1, если срок представления (выставления) платежных документов для внесения платы за жилое помещение и (или) коммунальные услуги в следующем месяце; иначе 0");
        col   ("ddt_i_start",               Type.NUMERIC,          2,           null,  "Срок внесения платы за жилое помещение и (или) коммунальные услуги (1..30 — конкретное число; 99 — последнее число)");
        col   ("ddt_i_start_nxt",           Type.BOOLEAN,                       null, "1, если срок внесения платы за жилое помещение и (или) коммунальные услуги в следующем месяце; иначе 0");

        col   ("contractbase",              Type.STRING,                        new Virt ("(''||\"CODE_VC_NSI_58\")"),  "Основание заключения договора");
        col   ("versionnumber",             Type.INTEGER,          10, null,    "Номер версии (по состоянию в ГИС ЖКХ)");
        col   ("reasonofannulment",         Type.STRING,         1000, null,    "Причина аннулирования");
        col   ("is_annuled",                Type.BOOLEAN,          new Virt ("DECODE(\"REASONOFANNULMENT\",NULL,0,1)"),  "1, если запись аннулирована; иначе 0");

       trigger ("BEFORE INSERT", "BEGIN "

           + "SELECT"
           + "       is_deleted"
           + "       , uuid_org"
           + "       , uuid_org_customer"
           + "       , id_customer_type"
           + "       , id_ctr_status"
           + "       , id_ctr_status_gis"
           + "       , docnum"
           + "       , signingdate"
           + "       , effectivedate"
           + "       , plandatecomptetion"
           + "       , automaticrolloveroneyear"
           + "       , code_vc_nsi_58"
           + "       , code_vc_nsi_54"
           + "       , terminate"
           + "       , ddt_m_start"
           + "       , ddt_m_start_nxt"
           + "       , ddt_m_end"
           + "       , ddt_m_end_nxt"
           + "       , ddt_d_start"
           + "       , ddt_d_start_nxt"
           + "       , ddt_i_start"
           + "       , ddt_i_start_nxt"
           + "       , reasonofannulment"
           + " INTO "
           + "       :NEW.is_deleted"
           + "       , :NEW.uuid_org"
           + "       , :NEW.uuid_org_customer"
           + "       , :NEW.id_customer_type"
           + "       , :NEW.id_ctr_status"
           + "       , :NEW.id_ctr_status_gis"
           + "       , :NEW.docnum"
           + "       , :NEW.signingdate"
           + "       , :NEW.effectivedate"
           + "       , :NEW.plandatecomptetion"
           + "       , :NEW.automaticrolloveroneyear"
           + "       , :NEW.code_vc_nsi_58"
           + "       , :NEW.code_vc_nsi_54"
           + "       , :NEW.terminate"
           + "       , :NEW.ddt_m_start"
           + "       , :NEW.ddt_m_start_nxt"
           + "       , :NEW.ddt_m_end"
           + "       , :NEW.ddt_m_end_nxt"
           + "       , :NEW.ddt_d_start"
           + "       , :NEW.ddt_d_start_nxt"
           + "       , :NEW.ddt_i_start"
           + "       , :NEW.ddt_i_start_nxt"
           + "       , :NEW.reasonofannulment"
           + " FROM tb_contracts WHERE uuid=:NEW.uuid_object; "

       + "END;");        
    }
    
}