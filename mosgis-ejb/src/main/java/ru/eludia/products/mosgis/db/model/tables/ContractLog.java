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
        col   ("automaticrolloveroneyear",  Type.BOOLEAN,                       null,   "1, если запись удалена; иначе 0");
        col   ("code_vc_nsi_58",            Type.STRING,           20,          null,   "Ссылка на НСИ \"Основание заключения договора\" (реестровый номер 58)");

        col   ("contractbase",              Type.STRING,                        new Virt ("(''||\"CODE_VC_NSI_58\")"),  "Основание заключения договора");

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
           + " FROM tb_contracts WHERE uuid=:NEW.uuid_object; "

       + "END;");        
    }
    
}