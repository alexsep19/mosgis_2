package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;

public class VocPersonLog extends Table {
    
    public VocPersonLog () {
        
        super ("vc_persons__log", "История редактирования физических лиц");
        
        pk    ("uuid",               Type.UUID,         NEW_UUID, "Ключ");
        ref   ("action",             VocAction.class,             "Действие");
        fk    ("uuid_object",        VocPerson.class,             "Ссылка на запись");
        col   ("ts",                 Type.TIMESTAMP,    NOW,      "Дата/время события");
        fk    ("uuid_user",          VocUser.class,     null,     "Оператор");
        
        col   ("is_deleted",         Type.BOOLEAN,      null,     "1, если запись удалена; иначе 0");
        
        col   ("is_female",          Type.BOOLEAN,      null,     "Пол");
        col   ("birthdate",          Type.DATE,         null,     "Дата рождения");
        
        col   ("snils",              Type.NUMERIC, 11,   null,    "СНИЛС");
        
        col   ("code_vc_nsi_95",     Type.STRING,  20,   null,    "Код документа, удостоверяющего личность (НСИ 95)");
        col   ("series",             Type.STRING,  45,   null,    "Серия документа, удостоверяющего дичность");
        col   ("number_",            Type.STRING,  45,   null,    "Номер документа, удостоверяющего личность");
        col   ("issuedate",          Type.DATE,          null,    "Дата выдачи документа, удостоверяющего личность");
        col   ("issuer",             Type.STRING,        null,    "Кем выдан документ, удостоверяющий личность");
        
        col   ("placebirth",         Type.STRING,  255,  null,    "Место рождения");
        
        col   ("surname",            Type.STRING,  256,           "Фамилия");
        col   ("firstname",          Type.STRING,  256,           "Имя");
        col   ("patronymic",         Type.STRING,  256,  null,    "Отчество");
        
        trigger ("BEFORE INSERT", 
                 "BEGIN "
                         + "SELECT "
                            + "is_deleted, "
                            + "is_female, "
                            + "birthdate, "
                            + "snils, "
                            + "code_vc_nsi_95, "
                            + "series, "
                            + "number_, "
                            + "issuedate, "
                            + "issuer, "
                            + "placebirth, "
                            + "surname, "
                            + "firstname, "
                            + "patronymic "
                         + "INTO "
                            + ":NEW.is_deleted, "
                            + ":NEW.is_female, "
                            + ":NEW.birthdate, "
                            + ":NEW.snils, "
                            + ":NEW.code_vc_nsi_95, "
                            + ":NEW.series, "
                            + ":NEW.number_, "
                            + ":NEW.issuedate, "
                            + ":NEW.issuer, "
                            + ":NEW.placebirth, "
                            + ":NEW.surname, "
                            + ":NEW.firstname, "
                            + ":NEW.patronymic "
                         + "FROM "
                            + "vc_persons "
                         + "WHERE "
                            + "uuid = :NEW.uuid_object; " +
                 "END; ");
    }
    
}
