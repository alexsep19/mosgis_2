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
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;

public class VocOrganizationProposal extends Table {

    public enum c implements EnColEnum {

        UUID                  (Type.UUID, NEW_UUID, "Ключ"),        
        ORGVERSIONGUID        (Type.UUID, null, "Идентификатор версии записи в реестре организаций"),
        PARENT                (VocOrganization.class, "Головная организация [обособленного подразделения]"),
        UUID_ORG              (VocOrganization.class, "Юридическое лицо, созданное по заявке, принятое из ГИС ЖКХ"),
        UUID_ORG_OWNER        (VocOrganization.class, "Организация оператора, создавшего запись"),
        IS_DELETED            (BOOLEAN, FALSE, "1, если запись удалена; иначе 0"),
        FULLNAME              (STRING, null, "Полное наименование"),
        SHORTNAME             (STRING, 500, null, "Краткое наименование"),

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

        // для ФПИЮЛ
        REGISTRATIONCOUNTRY    (VocOksm.class, null, "Страна регистрации"),
        NZA                    (NUMERIC, 11, null, "Номер записи об аккредитации"),
        ACCREDITATIONSTARTDATE (DATE, null, "Дата внесения в реестр аккредитованных"),
        ACCREDITATIONENDDATE   (DATE, null, "Дата прекращения аккредитации "),


        ID_TYPE              (VocOrganizationTypes.class, null, "Тип организации"),
        ID_LOG               (VocOrganizationProposalLog.class, null, "Последний запрос"),
        ID_ORG_PR_STATUS     (VocGisStatus.class, VocGisStatus.DEFAULT, "Статус заявки в mosgis"),
        ID_ORG_PR_STATUS_GIS (VocGisStatus.class, VocGisStatus.DEFAULT, "Статус заявки в ГИС ЖКХ"),
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
    
    public enum Action {
        
        PLACING     (VocGisStatus.i.PENDING_RP_PLACING,   VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_PLACING),
        ;
        
        VocGisStatus.i nextStatus;
        VocGisStatus.i okStatus;
        VocGisStatus.i failStatus;

        private Action (VocGisStatus.i nextStatus, VocGisStatus.i okStatus, VocGisStatus.i failStatus) {
            this.nextStatus = nextStatus;
            this.okStatus = okStatus;
            this.failStatus = failStatus;
        }

        public VocGisStatus.i getNextStatus () {
            return nextStatus;
        }

        public VocGisStatus.i getFailStatus () {
            return failStatus;
        }

        public VocGisStatus.i getOkStatus () {
            return okStatus;
        }

        public static Action forStatus (VocGisStatus.i status) {
            switch (status) {
                case PENDING_RQ_PLACING:   return PLACING;
                default: return null;
            }            
        }

        public static Action forLogAction (VocAction.i a) {
            switch (a) {
                case APPROVE: return PLACING;
                default: return null;
            }
        }

    };
    
}