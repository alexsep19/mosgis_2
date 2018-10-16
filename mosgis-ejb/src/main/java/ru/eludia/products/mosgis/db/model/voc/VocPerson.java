package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;

public class VocPerson extends Table {
    
    public VocPerson () {
        
        super ("vc_person", "Физические лица");
        
        pk  ("personuuid",  Type.UUID, "Ключ");
        
        col ("is_female",   Type.BOOLEAN,      null, "Пол");
        col ("birth_date",  Type.DATE,         null, "Дата рождения");
        
        col ("snils",       Type.NUMERIC, 11,        "СНИЛС");
        
        col ("type",        Type.STRING,  20,        "Код документа, удостоверяющего личность (НСИ 95)");
        col ("series",      Type.STRING,  45,  null, "Серия документа, удостоверяющего дичность");
        col ("number",      Type.STRING,  45,        "Номер документа, удостоверяющего личность");
        col ("issue_date",  Type.DATE,               "Дата выдачи документа, удостоверяющего личность");
        
        col ("place_birth", Type.STRING,  255, null, "Место рождения");
        
        col ("surname",     Type.STRING,  256,       "Фамилия");
        col ("first_name",  Type.STRING,  256,       "Имя");
        col ("patronymic",  Type.STRING,  256, null, "Отчество");
        
        col ("sex",         Type.STRING,  1, new Virt("DECODE(IS_FEMALE, TRUE, 'F', FALSE, 'M', NULL)"), "Пол (ГИС)");
        
    }
    
}
