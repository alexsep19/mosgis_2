package ru.eludia.products.mosgis.db.model.voc;

import java.util.UUID;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;
import ru.gosuslugi.dom.schema.integration.organizations_registry_base.ObjectFactory;
import ru.gosuslugi.dom.schema.integration.organizations_registry_base.RegOrgType;

public class VocOrganization extends Table {
    
    private static final ObjectFactory of = new ObjectFactory ();

    public VocOrganization () {

        super  ("vc_orgs",                                          "Юридические лица и частные предприниматели");

        pk     ("orgrootentityguid",  Type.UUID,                    "Ключ");

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
        
        col    ("uuid",           Type.UUID,    new Virt ("HEXTORAW(''||RAWTOHEX(\"ORGROOTENTITYGUID\"))"),  "uuid");
        col    ("label",          Type.STRING,        new Virt ("NVL(NVL(\"SHORTNAME\",\"FULLNAME\"), \"SURNAME\"||' '||\"FIRSTNAME\"||' '||\"PATRONYMIC\")"),  "Наименование");
        col    ("label_uc",       Type.STRING,  new Virt ("UPPER(NVL(NVL(\"SHORTNAME\",\"FULLNAME\"), \"SURNAME\"||' '||\"FIRSTNAME\"||' '||\"PATRONYMIC\"))"),  "НАИМЕНОВАНИЕ В ВЕРХНЕМ РЕГИСТРЕ");

        fk     ("id_type", VocOrganizationTypes.class, null, "Тип организации");
        
        fk     ("id_log",                    VocOrganizationLog.class,          null, "Последний запрос");

        key    ("label_uc", "label_uc");


        col("post_address", Type.STRING, null, "Почтовый адрес");

        col("phone", Type.STRING, null, "Телефон организации");

        col("mail", Type.STRING, null, "Электронный адрес");

        col("site", Type.STRING, null, "Адрес официального сайта");

        col("phone_support", Type.STRING, null, "Номер телефона горячей линии");


        col("head_fio", Type.STRING, null, "ФИО руководителя");

        col("head_post", Type.STRING, null, "Должность руководителя");

        col("head_phone", Type.STRING, null, "Телефон руководителя");

        col("head_mail", Type.STRING, null, "Электронный адрес руководителя");


        col("vice_fio", Type.STRING, null, "ФИО заместителя");

        col("vice_post", Type.STRING, null, "Должность заместителя");

        col("vice_phone", Type.STRING, null, "Телефон заместителя");

        col("vice_mail", Type.STRING, null, "Электронный адрес заместителя");


        col("citizen_address", Type.STRING, null, "Адрес приема граждан");

        col("citizen_phone", Type.STRING, null, "Телефон приема граждан");

        col("citizen_place", Type.STRING, null, "Место размещения информации для граждан");


        col("dispatch_address", Type.STRING, null, "Адрес диспетчерской службы");

        col("dispatch_phone", Type.STRING, null, "Контактные телефоны");

        col("dispatch_schedule", Type.STRING, null, "Режим работы");


        col("self_manage_org", Type.STRING, null, "Наименование саморегулируемой организации");

        col("dt_from_self_manage_org", Type.DATE, null, "Дата вступления в члены организации");

        col("dt_to_self_manage_org", Type.DATE, null, "Дата исключения/выхода из членов организации");

        col("reason_cancel_self_manage_org", Type.STRING, null, "Причина исключения из членов организации");


        col("staff_cnt", Type.INTEGER, null, "Количество штатных единиц");

        col("staff_work_cnt", Type.INTEGER, null, "Количество работающих человек");
    }
    
    public static final RegOrgType regOrgType (UUID uuid) {
        final RegOrgType o = of.createRegOrgType ();
        o.setOrgRootEntityGUID (uuid.toString ());
        return o;
    }

}