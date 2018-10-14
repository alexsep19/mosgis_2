package ru.eludia.products.mosgis.db.model.voc;

import java.util.UUID;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.gosuslugi.dom.schema.integration.organizations_registry_base.ObjectFactory;
import ru.gosuslugi.dom.schema.integration.organizations_registry_base.RegOrgType;

public class VocOrganizationLog extends Table {
    
    private static final ObjectFactory of = new ObjectFactory ();

    public VocOrganizationLog () {

        super  ("vc_orgs__log",                                     "Юридические лица и частные предприниматели: история");

        pk    ("uuid",                      Type.UUID,             NEW_UUID,            "Ключ");
        ref   ("action",                    VocAction.class,                            "Действие");
        fk    ("uuid_object",               VocOrganization.class,                      "Ссылка на запись");
        col   ("ts",                        Type.TIMESTAMP,        NOW,                 "Дата/время события");
        fk    ("uuid_user",                 VocUser.class,                      null,   "Оператор");
        fk    ("uuid_out_soap",             OutSoap.class,                      null,   "Последний запрос на импорт в ГИС ЖКХ");
        
        col   ("is_deleted",                Type.BOOLEAN,                       null,   "1, если запись удалена; иначе 0");
        
        col    ("orgppaguid",     Type.UUID,        null,           "Идентификатор зарегистрированной организации");

        col    ("shortname",      Type.STRING, 500, null,           "Сокращённое наименование");
        col    ("fullname",       Type.STRING,      null,           "Полное наименование");
        col    ("commercialname", Type.STRING,      null,           "Фирменное наименование");
        
        col    ("address",        Type.STRING,      null,           "Адрес регистрации");
        col    ("fiashouseguid",  Type.UUID,        null,           "Адрес регистрации (Глобальный уникальный идентификатор дома по ФИАС)");

        col    ("surname",        Type.STRING,      null,           "Фамилия");
        col    ("firstname",      Type.STRING,      null,           "Имя");
        col    ("patronymic",     Type.STRING,      null,           "Отчество");
        col    ("sex",            Type.STRING,  1,  null,           "Пол");
        
        col    ("ogrn",           Type.NUMERIC, 15, null,           "ОГРН");
        col    ("inn",            Type.NUMERIC, 12, null,           "ИНН");
        col    ("kpp",            Type.NUMERIC,  9, null,           "КПП");
        col    ("okopf",          Type.NUMERIC,  5, null,           "ОКОПФ");
        
        col    ("stateregistrationdate", Type.DATE, null,           "Дата государственной регистрации");
        col    ("activityenddate", Type.DATE,       null,           "Дата прекращения деятельности");
        
    }
    
    public static final RegOrgType regOrgType (UUID uuid) {
        final RegOrgType o = of.createRegOrgType ();
        o.setOrgRootEntityGUID (uuid.toString ());
        return o;
    }

}