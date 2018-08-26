package ru.eludia.products.mosgis.db.model.src;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.tables.Sender;
import static ru.eludia.base.model.def.Def.NEW_UUID;

public class SrcOrganization extends Table {

    public SrcOrganization () {
        
        super  ("src_orgs",                                         "Юридические лица (пользовательские справочники)");
        
        pk     ("uuid",           Type.UUID,   NEW_UUID,            "Ключ");
        
        col    ("label",          Type.STRING, 500,                 "Сокращённое наименование");
        col    ("label_full",     Type.STRING,                      "Полное наименование");
        col    ("address",        Type.STRING,       null,          "Адрес регистрации");
        col    ("uuid_fias",      Type.UUID,         null,          "Глобальный уникальный идентификатор дома по ФИАС");
 
        col    ("orgn",           Type.NUMERIC, 13,                 "ОГРН");
        col    ("inn",            Type.NUMERIC, 10,                 "ИНН");
        col    ("kpp",            Type.NUMERIC,  9,                 "КПП");
        col    ("okopf",          Type.NUMERIC,  5,                 "ОКОПФ");
        col    ("dt_to",          Type.DATE,     5,  null,          "Дата прекращения деятельности");

        ref    ("uuid_sender",    Sender.class,                     "Ссылка на поставщика данных");
        
        unique ("orgn_kpp", "uuid_sender", "orgn", "kpp");

    }
    
}