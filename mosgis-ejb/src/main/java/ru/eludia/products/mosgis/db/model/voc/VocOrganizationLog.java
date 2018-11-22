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


        col    ("post_address",   Type.STRING,     null, "Почтовый адрес");
        col    ("phone",          Type.STRING,     null, "Телефон организации");
        col    ("mail",           Type.STRING,     null, "Электронный адрес");
        col    ("site",           Type.STRING,     null, "Адрес официального сайта");
        col    ("phone_support",  Type.STRING,     null, "Номер телефона горячей линии");


        col   ("head_fio",        Type.STRING,     null, "ФИО руководителя");
        col   ("head_post",       Type.STRING,     null, "Должность руководителя");
        col   ("head_phone",      Type.STRING,     null, "Телефон руководителя");
        col   ("head_mail",       Type.STRING,     null, "Электронный адрес руководителя");


        col   ("vice_fio",        Type.STRING,     null, "ФИО заместителя");
        col   ("vice_post",       Type.STRING,     null, "Должность заместителя");
        col   ("vice_phone",      Type.STRING,     null, "Телефон заместителя");
        col   ("vice_mail",       Type.STRING,     null, "Электронный адрес заместителя");


        col   ("citizen_address", Type.STRING,     null, "Адрес приема граждан");
        col   ("citizen_phone",   Type.STRING,     null, "Телефон приема граждан");
        col   ("citizen_place",   Type.STRING,     null, "Место размещения информации для граждан");


        col   ("dispatch_address", Type.STRING,    null, "Адрес диспетчерской службы");
        col   ("dispatch_phone",   Type.STRING,    null, "Контактные телефоны");
        col   ("dispatch_schedule", Type.STRING,   null, "Режим работы");


        col   ("self_manage_org",   Type.STRING,   null, "Наименование саморегулируемой организации");
        col   ("dt_from_self_manage_org", Type.DATE, null, "Дата вступления в члены организации");
        col   ("dt_to_self_manage_org", Type.DATE, null, "Дата исключения/выхода из членов организации");
        col   ("reason_cancel_self_manage_org", Type.STRING, null, "Причина исключения из членов организации");


        col   ("staff_cnt",        Type.INTEGER,   null, "Количество штатных единиц");
        col   ("staff_work_cnt",   Type.INTEGER,   null, "Количество работающих человек");

        trigger("BEFORE INSERT", "BEGIN "
                + "SELECT "
                + "activityenddate, "
                + "address, "
                + "citizen_address, "
                + "citizen_phone, "
                + "citizen_place, "
                + "commercialname, "
                + "dispatch_address, "
                + "dispatch_phone, "
                + "dispatch_schedule, "
                + "dt_from_self_manage_org, "
                + "dt_to_self_manage_org, "
                + "fiashouseguid, "
                + "firstname, "
                + "fullname, "
                + "head_fio, "
                + "head_mail, "
                + "head_phone, "
                + "head_post, "
                + "inn, "
                + "is_deleted, "
                + "kpp, "
                + "mail, "
                + "ogrn, "
                + "okopf, "
                + "orgppaguid, "
                + "patronymic, "
                + "phone, "
                + "phone_support, "
                + "post_address, "
                + "reason_cancel_self_manage_org, "
                + "self_manage_org, "
                + "sex, "
                + "shortname, "
                + "site, "
                + "staff_cnt, "
                + "staff_work_cnt, "
                + "stateregistrationdate, "
                + "surname, "
                + "vice_fio, "
                + "vice_mail, "
                + "vice_phone, "
                + "vice_post "
                + " INTO "
                + ":NEW.activityenddate, "
                + ":NEW.address, "
                + ":NEW.citizen_address, "
                + ":NEW.citizen_phone, "
                + ":NEW.citizen_place, "
                + ":NEW.commercialname, "
                + ":NEW.dispatch_address, "
                + ":NEW.dispatch_phone, "
                + ":NEW.dispatch_schedule, "
                + ":NEW.dt_from_self_manage_org, "
                + ":NEW.dt_to_self_manage_org, "
                + ":NEW.fiashouseguid, "
                + ":NEW.firstname, "
                + ":NEW.fullname, "
                + ":NEW.head_fio, "
                + ":NEW.head_mail, "
                + ":NEW.head_phone, "
                + ":NEW.head_post, "
                + ":NEW.inn, "
                + ":NEW.is_deleted, "
                + ":NEW.kpp, "
                + ":NEW.mail, "
                + ":NEW.ogrn, "
                + ":NEW.okopf, "
                + ":NEW.orgppaguid, "
                + ":NEW.patronymic, "
                + ":NEW.phone, "
                + ":NEW.phone_support, "
                + ":NEW.post_address, "
                + ":NEW.reason_cancel_self_manage_org, "
                + ":NEW.self_manage_org, "
                + ":NEW.sex, "
                + ":NEW.shortname, "
                + ":NEW.site, "
                + ":NEW.staff_cnt, "
                + ":NEW.staff_work_cnt, "
                + ":NEW.stateregistrationdate, "
                + ":NEW.surname, "
                + ":NEW.vice_fio, "
                + ":NEW.vice_mail, "
                + ":NEW.vice_phone, "
                + ":NEW.vice_post "
                + " FROM vc_orgs WHERE uuid=:NEW.uuid_object; "
                + "END;"
        );
    }
    
    public static final RegOrgType regOrgType (UUID uuid) {
        final RegOrgType o = of.createRegOrgType ();
        o.setOrgRootEntityGUID (uuid.toString ());
        return o;
    }

}