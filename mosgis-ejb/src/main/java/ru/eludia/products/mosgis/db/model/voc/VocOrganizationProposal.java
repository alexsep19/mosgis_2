package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.BOOLEAN;
import static ru.eludia.base.model.Type.DATE;
import static ru.eludia.base.model.Type.NUMERIC;
import static ru.eludia.base.model.Type.STRING;
import static ru.eludia.base.model.def.Bool.FALSE;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Num;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;

public class VocOrganizationProposal extends Table {

    public enum c implements EnColEnum {

        UUID                  (Type.UUID, NEW_UUID, "Ключ"),
        PARENT                (VocOrganization.class, "Головная организация [обособленного подразделения]"),
        UUID_ORG              (VocOrganization.class, "Юридическое лицо, созданное по заявке, принятое из ГИС ЖКХ"),
        IS_DELETED            (BOOLEAN, FALSE, "1, если запись удалена; иначе 0"),
        FULLNAME              (STRING, null, "Полное наименование"),
        SHORTNAME             (STRING, null, "Краткое наименование"),

        ADDRESS               (STRING, null, "Адрес регистрации"),
        FIASHOUSEGUID         (VocBuildingAddress.class, "Адрес регистрации ФИАС"),

        OGRN                  (NUMERIC, 15, null, "ОГРН"),
        INN                   (NUMERIC, 12, null, "ИНН"),
        KPP                   (NUMERIC, 9, null, "КПП"),
        OKOPF                 (NUMERIC, 5, null, "ОКОПФ"),

        STATEREGISTRATIONDATE(DATE, null, "Дата государственной регистрации"),
        ACTIVITYENDDATE      (DATE, null, "Дата прекращения деятельности"),
        LABEL                (STRING, new Virt("NVL(\"SHORTNAME\",\"FULLNAME\")"), "Наименование"),
        LABEL_UC             (STRING, new Virt("UPPER(NVL(\"SHORTNAME\",\"FULLNAME\"))"), "НАИМЕНОВАНИЕ В ВЕРХНЕМ РЕГИСТРЕ"),
        INFO_SOURCE          (STRING, null, "Источник данных"),
        DT_INFO_SOURCE       (DATE, null, "Дата получения информации от источника данных"),

        ID_TYPE              (VocOrganizationTypes.class, null, "Тип организации"),
        ID_LOG               (VocOrganizationProposalLog.class, null, "Последний запрос"),
        ID_ORG_PR_STATUS     (VocGisStatus.class, new Num(VocGisStatus.i.PROJECT.getId()), "Статус заявки");
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
                case IS_DELETED:
                case UUID:
                case ID_LOG:
                case ID_TYPE:
                case FIASHOUSEGUID:
                    return false;
                default:
                    return true;
            }
        }
    }

    public VocOrganizationProposal() {

        super  ("vc_org_proposals", "Юридические лица: заявки на добавление [обособленных подразделений и ФПИЮЛ] в ГИС ЖКХ");

        cols   (c.class);

        pk     (c.UUID);

        key    ("parent", "parent");
        key    ("uuid_org", "uuid_org");
        key    ("label_uc", "label_uc");
    }
}