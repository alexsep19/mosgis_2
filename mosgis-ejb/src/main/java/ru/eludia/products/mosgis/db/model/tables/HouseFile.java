package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.db.sql.build.QP;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Blob.EMPTY_BLOB;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Num.ZERO;
import ru.eludia.products.mosgis.db.model.voc.VocPassportDocFields;

public class HouseFile extends Table {

    public HouseFile () {
        
        super  ("tb_house_files", "Файлы, приложенные к паспортам МКД/ЖД");
        
        pk     ("uuid",                  Type.UUID, NEW_UUID,  "Ключ");
        
        ref    ("uuid_house",            House.class,                "Ссылка на МКД/ЖД");
        ref    ("uuid_premise_res",      ResidentialPremise.class,   null, "Ссылка на жилое помещение");
        ref    ("uuid_block",            Block.class,                null, "Ссылка на блок ЖД");
        ref    ("uuid_living_room",      LivingRoom.class,           null, "Ссылка на комнату");
        
        ref    ("id_type",               VocPassportDocFields.class, "Ссылка на тип документа");
        
        col    ("label",                 Type.STRING,  "Имя файла");
        col    ("mime",                  Type.STRING,  "Тип содержимого");
        col    ("len",                   Type.INTEGER, "Размер, байт");
        col    ("body",                  Type.BLOB, EMPTY_BLOB, "Содержимое");
        col    ("note",                  Type.TEXT, null, "Примечание");

        col    ("id_status",             Type.INTEGER, 1, ZERO, "Статус");
        
        trigger ("BEFORE INSERT", "BEGIN "

                + " IF :NEW.uuid_premise_res IS NOT NULL THEN "
                + "  BEGIN"
                + "   SELECT uuid_house INTO :NEW.uuid_house FROM tb_premises_res WHERE uuid = :NEW.uuid_premise_res; "
                + "  END;"
                + " END IF;"

                + " IF :NEW.uuid_block IS NOT NULL THEN "
                + "  BEGIN"
                + "   SELECT uuid_house INTO :NEW.uuid_house FROM tb_blocks WHERE uuid = :NEW.uuid_block; "
                + "  END;"
                + " END IF;"
                
                + " IF :NEW.uuid_living_room IS NOT NULL THEN "
                + "  BEGIN"
                + "   SELECT uuid_house INTO :NEW.uuid_house FROM tb_living_rooms WHERE uuid = :NEW.uuid_living_room; "
                + "  END;"
                + " END IF;"

                + "END;");        

    }
    
    public QP getStatusUpdateBuilder (String uuid) {
        QP qp = new QP ("UPDATE ");
        qp.append (name);
        qp.append (" SET id_status = CASE WHEN DBMS_LOB.GETLENGTH(body) = len THEN 1 ELSE 0 END WHERE uuid = ");
        qp.add ("?", uuid, getColumn ("uuid").toPhysical ());
        return qp;
    }
    
}