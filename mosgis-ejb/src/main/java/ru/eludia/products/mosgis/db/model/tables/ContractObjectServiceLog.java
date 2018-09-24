package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.model.voc.VocUser;

public class ContractObjectServiceLog extends Table {

    public ContractObjectServiceLog () {

        super  ("tb_contract_services__log", "История редактирования услуг по договору");

        pk    ("uuid",                      Type.UUID,             NEW_UUID,            "Ключ");
        col   ("action",                    Type.STRING,                                "Действие");
        fk    ("uuid_object",               ContractObjectService.class,     "Ссылка на запись");
        col   ("ts",                        Type.TIMESTAMP,        NOW,                 "Дата/время события");
        fk    ("uuid_user",                 VocUser.class,                      null,   "Оператор");
        
        fk    ("uuid_out_soap",             OutSoap.class,                      null,   "Последний запрос на импорт в ГИС ЖКХ");
        col   ("uuid_message",              Type.UUID,                          null,   "UUID запроса в ГИС ЖКХ");
//        col   ("...",           Type.UUID,                          null,   "Идентификатор новой версии");        

        col    ("is_deleted",              Type.BOOLEAN,            null,       "1, если запись удалена; иначе 0");
        ref    ("uuid_contract_agreement", ContractFile.class,      null,       "Ссылка на дополнительное соглашение");        
        col    ("code_vc_nsi_3",           Type.STRING,  20,        null,       "Коммунальная услуга");
        ref    ("uuid_add_service",        AdditionalService.class, null,       "Дополнительная услуга");
        col    ("startdate",               Type.DATE,               null,       "Дата начала предоставления услуги");
        col    ("enddate",                 Type.DATE,               null,       "Дата окончания предоставления услуги");

       trigger ("BEFORE INSERT", "BEGIN "

           + "SELECT"
           + "       is_deleted,              "
           + "       uuid_contract_agreement, "
           + "       code_vc_nsi_3,           "
           + "       uuid_add_service,        "
           + "       startdate,               "
           + "       enddate                  "
           + "INTO "                
           + "       :NEW.is_deleted,         "
           + "       :NEW.uuid_contract_agreement, "
           + "       :NEW.code_vc_nsi_3,           "
           + "       :NEW.uuid_add_service,        "
           + "       :NEW.startdate,               "
           + "       :NEW.enddate                  "
           + " FROM "
           + "  tb_contract_services "
           + " WHERE uuid=:NEW.uuid_object; "

       + "END;");
       
    }

}