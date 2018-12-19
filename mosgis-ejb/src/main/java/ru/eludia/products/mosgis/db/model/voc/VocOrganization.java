package ru.eludia.products.mosgis.db.model.voc;

import java.util.UUID;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.BOOLEAN;
import static ru.eludia.base.model.Type.DATE;
import static ru.eludia.base.model.Type.NUMERIC;
import static ru.eludia.base.model.Type.INTEGER;
import static ru.eludia.base.model.Type.STRING;
import static ru.eludia.base.model.def.Bool.FALSE;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.gosuslugi.dom.schema.integration.organizations_registry_base.ObjectFactory;
import ru.gosuslugi.dom.schema.integration.organizations_registry_base.RegOrgType;

public class VocOrganization extends Table {

    private static final ObjectFactory of = new ObjectFactory ();

    public enum c implements EnColEnum {

        ORGPPAGUID     (Type.UUID,   null,           "Идентификатор зарегистрированной организации"),

        SHORTNAME      (STRING, 500, null,           "Сокращённое наименование"),
        FULLNAME       (STRING,      null,           "Полное наименование"),
        COMMERCIALNAME (STRING,      null,           "Фирменное наименование"),

        ADDRESS        (STRING,      null,           "Адрес регистрации"),
        FIASHOUSEGUID  (Type.UUID,        null,           "Адрес регистрации (Глобальный уникальный идентификатор дома по ФИАС)"),

        SURNAME        (STRING,      null,           "Фамилия"),
        FIRSTNAME      (STRING,      null,           "Имя"),
        PATRONYMIC     (STRING,      null,           "Отчество"),
        SEX            (STRING,  1,  null,           "Пол"),

        OGRN           (NUMERIC, 15, null,           "ОГРН"),
        INN            (NUMERIC, 12, null,           "ИНН"),
        KPP            (NUMERIC,  9, null,           "КПП"),
        OKOPF          (NUMERIC,  5, null,           "ОКОПФ"),

        STATEREGISTRATIONDATE (DATE, null,           "Дата государственной регистрации"),
        ACTIVITYENDDATE (DATE,       null,           "Дата прекращения деятельности"),

        UUID           (Type.UUID,    new Virt ("HEXTORAW(''||RAWTOHEX(\"ORGROOTENTITYGUID\"))"),  "uuid"),
        LABEL          (STRING,        new Virt ("NVL(NVL(\"SHORTNAME\",\"FULLNAME\"), \"SURNAME\"||' '||\"FIRSTNAME\"||' '||\"PATRONYMIC\")"),  "Наименование"),
        LABEL_UC       (STRING,  new Virt ("UPPER(NVL(NVL(\"SHORTNAME\",\"FULLNAME\"), \"SURNAME\"||' '||\"FIRSTNAME\"||' '||\"PATRONYMIC\"))"),  "НАИМЕНОВАНИЕ В ВЕРХНЕМ РЕГИСТРЕ"),

        IS_DELETED     (BOOLEAN, FALSE, "1, если запись удалена; иначе 0"),


        POST_ADDRESS   (STRING,     null, "Почтовый адрес"),
        PHONE          (STRING,     null, "Телефон организации"),
        MAIL           (STRING,     null, "Электронный адрес"),
        SITE           (STRING,     null, "Адрес официального сайта"),
        PHONE_SUPPORT  (STRING,     null, "Номер телефона горячей линии"),


        HEAD_FIO        (STRING,     null, "ФИО руководителя"),
        HEAD_POST       (STRING,     null, "Должность руководителя"),
        HEAD_PHONE      (STRING,     null, "Телефон руководителя"),
        HEAD_MAIL       (STRING,     null, "Электронный адрес руководителя"),


        VICE_FIO        (STRING,     null, "ФИО заместителя"),
        VICE_POST       (STRING,     null, "Должность заместителя"),
        VICE_PHONE      (STRING,     null, "Телефон заместителя"),
        VICE_MAIL       (STRING,     null, "Электронный адрес заместителя"),


        CITIZEN_ADDRESS (STRING,     null, "Адрес приема граждан"),
        CITIZEN_PHONE   (STRING,     null, "Телефон приема граждан"),
        CITIZEN_PLACE   (STRING,     null, "Место размещения информации для граждан"),


        DISPATCH_ADDRESS (STRING,    null, "Адрес диспетчерской службы"),
        DISPATCH_PHONE   (STRING,    null, "Контактные телефоны"),
        DISPATCH_SCHEDULE (STRING,   null, "Режим работы"),


        SLF_MNG_ORG   (STRING,   null, "Наименование саморегулируемой организации"),
        DT_FROM_SLF_MNG_ORG (DATE, null, "Дата вступления в члены организации"),
        DT_TO_SLF_MNG_ORG (DATE, null, "Дата исключения/выхода из членов организации"),
        RSN_SLF_MNG_ORG (STRING, null, "Причина исключения из членов организации"),

        STAFF_CNT        (INTEGER,   null, "Количество штатных единиц"),
        STAFF_WORK_CNT   (INTEGER,   null, "Количество работающих человек"),
        ;

        @Override
        public Col getCol() {
            return col;
        }
        private Col col;

        private c(Type type, Object... p) {
            col = new Col(this, type, p);
        }

        private c(Class c, Object... p) {
            col = new Ref(this, c, p);
        }

        @Override
        public boolean isLoggable() {
            switch (this) {
                case ORGPPAGUID:
                case UUID:
                case FIASHOUSEGUID:
                case LABEL:
                case LABEL_UC:
                    return false;
                default:
                    return true;
            }
        }
    }

    public VocOrganization () {

        super  ("vc_orgs", "Юридические лица и частные предприниматели");

        cols(c.class);

        pk("orgrootentityguid",  Type.UUID, "Ключ");

        fk("id_type", VocOrganizationTypes.class, null, "Тип организации");

        fk("id_log", VocOrganizationLog.class, null, "Последний запрос");

        key("label_uc", "label_uc");
    }

    public static final RegOrgType regOrgType (UUID uuid) {
        final RegOrgType o = of.createRegOrgType ();
        o.setOrgRootEntityGUID (uuid.toString ());
        return o;
    }

}