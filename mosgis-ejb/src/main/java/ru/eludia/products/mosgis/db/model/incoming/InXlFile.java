package ru.eludia.products.mosgis.db.model.incoming;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import static ru.eludia.base.model.def.Blob.EMPTY_BLOB;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.model.voc.VocFileStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocUser;
import ru.eludia.products.mosgis.db.model.voc.VocXLFileType;

public class InXlFile extends EnTable {

    public enum c implements EnColEnum {

        UUID_USER      (VocUser.class,                              "Оператор"),
        UUID_ORG       (VocOrganization.class,                      "Организация"),
        LABEL          (Type.STRING, 1024,                          "Имя файла"),
        MIME           (Type.STRING,                                "Тип содержимого"),
        LEN            (Type.INTEGER,                               "Размер"),
        BODY           (Type.BLOB,              EMPTY_BLOB,         "Содержимое"),
        TS             (Type.TIMESTAMP,         NOW,                "Дата начала импорта"),
        ERR            (Type.TEXT,              new ru.eludia.base.model.def.String ("	Ошибки импорта "),  "Ошибки"),
        ID_TYPE        (VocXLFileType.class,                        "Тип"),
        ID_STATUS      (VocFileStatus.class, VocFileStatus.DEFAULT, "Статус"),
        ID_LOG         (InXlFileLog.class,                          "Последнее событие редактирования"),
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
            return this == ID_STATUS;
        }

    }

    public InXlFile () {

        super ("in_xl_files", "Файлы импорта");

        cols  (c.class);
        
        key ("ts", c.TS);
        key ("uuid_org_ts", c.UUID_ORG, c.TS);

        trigger ("BEFORE INSERT",
            "BEGIN "
            + " SELECT uuid_org INTO :NEW.uuid_org FROM vc_users WHERE uuid=:NEW.uuid_user; "
            + "END;"
        );

        trigger ("BEFORE UPDATE", 
            "BEGIN "
            + " IF :NEW.id_status = 0 AND DBMS_LOB.GETLENGTH (:NEW.body) = :NEW.len THEN "
            + "   :NEW.id_status := 1; "
            + " END IF;"
            + "END;"
        );

    }

}