package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Virt;

public class VocPerson extends Table {
    
    public VocPerson () {
        
        super ("vc_persons", "Физические лица");
        
        pk  ("uuid",     Type.UUID,         NEW_UUID, "Ключ");
        
        fk  ("uuid_org",       VocOrganization.class,          "Поставщик информации");
        
        col ("is_deleted",     Type.BOOLEAN,       Bool.FALSE, "1, если запись удалена; иначе 0");
        
        col ("is_female",      Type.BOOLEAN,       null,       "Пол");
        col ("birthdate",      Type.DATE,          null,       "Дата рождения");
        
        col ("snils",          Type.NUMERIC, 11,   null,       "СНИЛС");
        
        col ("code_vc_nsi_95", Type.STRING,  20,   null,      "Код документа, удостоверяющего личность (НСИ 95)");
        col ("series",         Type.STRING,  45,   null,      "Серия документа, удостоверяющего дичность");
        col ("number_",        Type.STRING,  45,   null,      "Номер документа, удостоверяющего личность");
        col ("issuedate",      Type.DATE,          null,      "Дата выдачи документа, удостоверяющего личность");
        col ("issuer",         Type.STRING,        null,      "Кем выдан документ, удостоверяющий личность");
        
        col ("placebirth",     Type.STRING,  255,  null,      "Место рождения");
        
        col ("surname",        Type.STRING,  256,             "Фамилия");
        col ("firstname",      Type.STRING,  256,             "Имя");
        col ("patronymic",     Type.STRING,  256,  null,      "Отчество");
        
        col ("label",          Type.STRING,     new Virt("DECODE(\"PATRONYMIC\", NULL, (\"SURNAME\" || ' ' || \"FIRSTNAME\"), (\"SURNAME\" || ' ' || \"FIRSTNAME\" || ' ' || \"PATRONYMIC\"))"), "ФИО");
        col ("label_uc",       Type.STRING,     new Virt("UPPER(\"LABEL\")"), "ФИО (в верхнем регистре)");
        
        col ("sex",            Type.STRING,  1, new Virt("DECODE(IS_FEMALE, 1, 'F', 0, 'M', NULL)"), "Пол (ГИС)");
        
        fk  ("id_log",         VocPersonLog.class, null,      "Последнее событие редактирования");
        
        key ("uuid_org", "uuid_org");
        
    }
    
}
