package ru.eludia.products.mosgis.db.model.incoming.rec;

import ru.eludia.products.mosgis.db.model.incoming.json.InImportSubsidiaries;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.model.src.SrcOrganization;

public class InCreateSubsidiary extends Table {

    public InCreateSubsidiary () {
        
        super  ("in_tb_cr_subsidiaries",                            "Входящие запросы на регистрацию отдельных новых юрлиц");
        
        pk     ("uuid",           Type.UUID,                        "Ключ (TransportGUID)");
        
        col    ("label",          Type.STRING, 500,                 "Сокращённое наименование");
        col    ("label_full",     Type.STRING,                      "Полное наименование");
        col    ("address",        Type.STRING,       null,          "Адрес регистрации");
        col    ("uuid_fias",      Type.UUID,         null,          "Глобальный уникальный идентификатор дома по ФИАС");
 
        col    ("orgn",           Type.NUMERIC, 13,                 "ОГРН");
        col    ("inn",            Type.NUMERIC, 10,                 "ИНН");
        col    ("kpp",            Type.NUMERIC,  9,                 "КПП");
        col    ("okopf",          Type.NUMERIC,  5,                 "ОКОПФ");
        
        col    ("dt_to",          Type.DATE,     5,  null,          "Дата прекращения деятельности");
        col    ("ts",             Type.TIMESTAMP,    NOW,           "Дата/время записи в БД");
        col    ("ts_done",        Type.TIMESTAMP,    null,          "Дата/время обработки");
        col    ("error",          Type.STRING,       null,          "Текст ошибки");
        
        ref    ("uuid_in_import", InImportSubsidiaries.class,  "Ссылка на охватывающий пакетный запрос");
        ref    ("uuid_src_org",   SrcOrganization.class, null,      "Ссылка на результат регистрации");
        
    }
    
}