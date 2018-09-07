package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Virt;

public class ContractObjectService extends Table {

    public ContractObjectService () {

        super  ("tb_contract_services", "Услуги по договору");

        pk     ("uuid",                    Type.UUID,               NEW_UUID,   "Ключ");
        col    ("is_deleted",              Type.BOOLEAN,            Bool.FALSE, "1, если запись удалена; иначе 0");

        ref    ("uuid_contract_object",    ContractObject.class,                "Ссылка на объект договора");
        fk     ("uuid_contract",           Contract.class,                      "Ссылка на договор");
        
        ref    ("uuid_contract_agreement", ContractFile.class,      null,       "Ссылка на дополнительное соглашение");
        
        col    ("code_vc_nsi_3",           Type.STRING,  20,        null,       "Коммунальная услуга");
        ref    ("uuid_add_service",        AdditionalService.class, null,       "Дополнительная услуга");

        col    ("is_additional",           Type.BOOLEAN,            new Virt    ("DECODE(\"UUID_ADD_SERVICE\",NULL,0,1)"),  "1, для дополнительной услуги, 0 для коммунальной");

        col    ("startdate",               Type.DATE,                           "Дата начала предоставления услуги");
        col    ("enddate",                 Type.DATE,                           "Дата окончания предоставления услуги");

        trigger ("BEFORE INSERT", "BEGIN "
            + "SELECT uuid_contract INTO :NEW.uuid_contract FROM tb_contract_objects WHERE uuid = :NEW.uuid_contract_object; "
        + "END;");

    }

}