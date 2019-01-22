package ru.eludia.products.mosgis.db.model.incoming.xl.lines;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.tables.Contract;
import ru.eludia.products.mosgis.db.model.tables.ContractFile;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class InXlContractObject extends EnTable {

    public enum c implements ColEnum {

        UUID_XL                 (InXlFile.class,            "Файл импорта"),
        ORD                     (Type.NUMERIC, 5,           "Номер строки"),
        
        UUID_ORG                (VocOrganization.class,     "Организация-исполнитель договора"),

        ADDRESS                 (Type.STRING, null,         "Адрес"),
        DOCNUM                  (Type.STRING, null,         "Номер договора"),
        SIGNINGDATE             (Type.DATE, null,           "Дата заключения"),

        UUID_CONTRACT           (Contract.class,            "Ссылка на договор"),
        UUID_CONTRACT_AGREEMENT (ContractFile.class,        "Ссылка на дополнительное соглашение"),        
        
        UNOM                    (Type.NUMERIC, 15, null,    "UNOM"),
        FIASHOUSEGUID           (VocBuilding.class,         "Код ФИАС"),
        
        STARTDATE               (Type.DATE, null,           "Дата начала предоставления услуг"),
        ENDDATE                 (Type.DATE, null,           "Дата окончания предоставления услуг"),
        
        AGREEMENTNUMBER         (Type.STRING, null,         "Номер дополнительного соглашения"),
        AGREEMENTDATE           (Type.DATE, null,           "Дата дополнительного соглашения"),
        
        ERR                     (Type.STRING,  null,        "Ошибка"),

        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

    }

    public InXlContractObject () {

        super ("in_xl_ctr_objects", "Строки импорта объектов управления");

        cols  (c.class);
        
        key ("uuid_xl", c.UUID_XL);

//        trigger ("BEFORE INSERT",
//            "BEGIN "
////            + " SELECT uuid_org INTO :NEW.uuid_org FROM vc_users WHERE uuid=:NEW.uuid_user; "
//            + "END;"
//        );

    }

}