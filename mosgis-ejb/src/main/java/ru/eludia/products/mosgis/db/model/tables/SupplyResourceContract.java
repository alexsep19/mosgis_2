package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.eludia.products.mosgis.db.model.voc.VocGisContractDimension;
import ru.eludia.products.mosgis.db.model.voc.VocGisSupplyResourceContractCustomerType;


public class SupplyResourceContract extends EnTable {

    public enum c implements EnColEnum {

        UUID_ORG             (VocOrganization.class,     "Организация-исполнитель"),
        UUID_ORG_CUSTOMER    (VocOrganization.class,     "Организация-заказчик"),
        UUID_PERSON_CUSTOMER (VocPerson.class,           "Физлицо-заказчик"),
        ID_CUSTOMER_TYPE     (VocGisSupplyResourceContractCustomerType.class, "Тип заказчика"),

        ID_CTR_STATUS        (VocGisStatus.class,        VocGisStatus.i.PROJECT.asDef (),     "Статус договора с точки зрения mosgis"),
        ID_CTR_STATUS_GIS    (VocGisStatus.class,        VocGisStatus.i.PROJECT.asDef (),     "Статус договора с точки зрения ГИС ЖКХ"),
        ID_CTR_STATE_GIS     (VocGisStatus.class,        VocGisStatus.i.NOT_RUNNING.asDef (), "Состояние договора с точки зрения ГИС ЖКХ"),

        ID_LOG               (SupplyResourceContractLog.class,  "Последнее событие редактирования"),

        CONTRACTNUMBER       (Type.STRING, 255, "Номер договора"),
        SIGNINGDATE          (Type.DATE, "Дата заключения"),
        EFFECTIVEDATE        (Type.DATE, "Дата вступления в силу"),
        COMPLETIONDATE       (Type.DATE, null, "Дата окончания действия"),
        AUTOROLLOVER         (Type.BOOLEAN, null, "1, если автопролонгация на год; иначе 0"),

	LABEL                (Type.STRING, new Virt("'№' || contractnumber || ' от ' || TO_CHAR (signingdate, 'DD.MM.YYYY')"), "№/дата"),

        CODE_VC_NSI_58       (Type.STRING, 20, "Основание заключения договора (НСИ 58)"),
        IS_CONTRACT          (Type.BOOLEAN,    null, "1, если является публичным; иначе 0"),

        ONETIMEPAYMENT       (Type.BOOLEAN, null, "1, если единоразовая оплата услуг; иначе 0"),
        COUNTINGRESOURCE     (Type.BOOLEAN, null, "1, если РСО размещает информацию о начислениях за коммунальные услуги; 0 - размещает Исполнитель коммунальных услуг."),
        ISPLANNEDVOLUME      (Type.BOOLEAN, null, "1, если в договоре есть плановый объем и режим подачи поставки ресурсов; иначе 0"),

        PLANNEDVOLUMETYPE    (VocGisContractDimension.class
                , VocGisContractDimension.i.BY_CONTRACT.asDef()
                , "Тип ведения планового объема и режима подачи в разрезе"
        ),
        ACCRUALPROCEDURE     (VocGisContractDimension.class
                , VocGisContractDimension.i.BY_CONTRACT.asDef()
                , "Порядок размещения информации о начислениях за коммунальные услуги в разрезе"
        ),
        SPECQTYINDS          (VocGisContractDimension.class
                , VocGisContractDimension.i.BY_CONTRACT.asDef()
                , "Показатели качества коммунальных ресурсов и температурный график указываются в разрезе"
        ),
        VOLUMEDEPENDS        (Type.BOOLEAN, null, "1, если Объем поставки ресурса(ов) определяется на основании прибора учета; иначе 0"),

        MDINFO               (Type.BOOLEAN, null, "1, если РСО размещает информацию об индивидуальных приборах учета и их показаниях; иначе 0"),


        DDT_M_START          (Type.NUMERIC, 2, null, "Начало периода ввода показаний ПУ (1..31 — конкретное число, 99 — последнее число)"),
        DDT_M_START_NXT      (Type.BOOLEAN,    null, "1, если начало периода ввода показаний ПУ в следующем месяце; иначе 0"),
        DDT_M_END            (Type.NUMERIC, 2, null, "Окончание периода ввода показаний ПУ (1..31 — конкретное число; 99 — последнее число)"),
        DDT_M_END_NXT        (Type.BOOLEAN,    null, "1, если окончание периода ввода показаний ПУ в следующем месяце; иначе 0"),

        DDT_D_START          (Type.NUMERIC, 2, null, "Срок представления (выставления) платежных документов для внесения платы за жилое помещение и (или) коммунальные услуги (1..30 — конкретное число; 99 — последнее число)"),
        DDT_D_START_NXT      (Type.BOOLEAN,    null, "1, если срок представления (выставления) платежных документов для внесения платы за жилое помещение и (или) коммунальные услуги в следующем месяце; иначе 0"),

        DDT_I_START          (Type.NUMERIC, 2, null, "Срок внесения платы за жилое помещение и (или) коммунальные услуги (1..30 — конкретное число; 99 — последнее число)"),
        DDT_I_START_NXT      (Type.BOOLEAN,    null, "1, если срок внесения платы за жилое помещение и (или) коммунальные услуги в следующем месяце; иначе 0"),

        DDT_N_START          (Type.NUMERIC, 2, null, "Срок предоставления информации о поступивших платежах, не позднее (1..30 — конкретное число; 99 — последнее число)"),
        DDT_N_START_NXT      (Type.BOOLEAN,    null, "1, если срок предоставления информации о поступивших платежах в следующем месяце; иначе 0"),

	REASONOFANNULMENT    (Type.STRING, 1000, null, "Причина аннулирования"),
	IS_ANNULED           (Type.BOOLEAN, new Virt("DECODE(\"REASONOFANNULMENT\",NULL,0,1)"), "1, если запись аннулирована; иначе 0"),

	CONTRACTGUID         (Type.UUID, null, "Идентификатор версии ДРСО в ГИС ЖКХ"),
	CONTRACTROOTGUID     (Type.UUID, null, "Идентификатор ДРСО в ГИС ЖКХ")
        ;

        @Override
        public Col getCol () {return col;}
        private Col col;
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
            switch (this) {
                case ID_LOG:
                case UUID_ORG:
                case UUID_ORG_CUSTOMER:
                case UUID_PERSON_CUSTOMER:
                    return false;
                default:
                    return true;
            }
        }

    }

    public SupplyResourceContract () {

        super ("tb_sr_ctr", "Договор ресурсоснабжения");

        cols   (c.class);

        key    ("uuid_org", c.UUID_ORG);

        trigger("BEFORE INSERT OR UPDATE", ""
//                + "DECLARE"
//                + " PRAGMA AUTONOMOUS_TRANSACTION; "
                + "BEGIN "

                + " IF :NEW.is_deleted = 0 THEN "
                    + " IF :NEW.completiondate IS NULL AND :NEW.autorollover = 1 THEN "
                    + "   raise_application_error (-20000, 'Укажите дату окончания или Автопролонгация нет. Операция отменена.'); "
                    + " END IF; "

                    + " IF :NEW.DDT_D_START > :NEW.DDT_I_START AND :NEW.DDT_D_START_NXT = :NEW.DDT_I_START_NXT"
                    + "   OR :NEW.DDT_D_START_NXT > :NEW.DDT_I_START_NXT "
                    + " THEN "
                    + "   raise_application_error (-20000, 'Срок выставления платежных документов не может превышать срок внесения платы. Операция отменена.'); "
                    + " END IF; "

                    + " IF :NEW.DDT_M_START > :NEW.DDT_M_END AND :NEW.DDT_M_START_NXT = :NEW.DDT_M_END_NXT"
                    + "   OR :NEW.DDT_M_START_NXT > :NEW.DDT_M_END_NXT "
                    + " THEN "
                    + "   raise_application_error (-20000, 'Начало ввода показаний ПУ не может превышать окончание ввода показаний ПУ. Операция отменена.'); "
                    + " END IF; "

                    + " FOR i IN ("
                        + "SELECT "
                        + " o.startsupplydate "
                        + " , o.endsupplydate "
                        + " , nsi_3.f_d966dd6cbc   nsi_3_label "
                        + " , nsi_239.f_adebb17ebe nsi_239_label "
                        + "FROM "
                        + " tb_sr_ctr_subj o "
                        + " INNER JOIN vc_nsi_3   nsi_3   ON o.code_vc_nsi_3   = nsi_3.code "
                        + " INNER JOIN vc_nsi_239 nsi_239 ON o.code_vc_nsi_239 = nsi_239.code "
                        + "WHERE o.is_deleted = 0 "
                        + " AND o.uuid_sr_ctr     = :NEW.uuid "
                        + " AND (o.startsupplydate < :NEW.effectivedate) "
                    + ") LOOP"
                        + " raise_application_error (-20000, "
                        + "'Некорректная дата вступления в силу договора ресурсоснабжения. Коммунальная услуга ' || i.nsi_239_label "
                        + "|| ' (коммунальный ресурс ' || i.nsi_3_label || ') поставляется с ' "
                        + "|| TO_CHAR (i.startsupplydate, 'DD.MM.YYYY') "
                        + "|| CASE WHEN i.endsupplydate IS NULL THEN NULL ELSE ' по ' "
                        + "|| TO_CHAR (i.endsupplydate, 'DD.MM.YYYY') END "
                        + "|| '. Операция отменена.'); "
                    + " END LOOP; "
 
                    + " FOR i IN ("
                        + "SELECT "
                        + " o.startsupplydate "
                        + " , o.endsupplydate "
                        + " , nsi_3.f_d966dd6cbc   nsi_3_label "
                        + " , nsi_239.f_adebb17ebe nsi_239_label "
                        + "FROM "
                        + " tb_sr_ctr_subj o "
                        + " INNER JOIN vc_nsi_3   nsi_3   ON o.code_vc_nsi_3   = nsi_3.code "
                        + " INNER JOIN vc_nsi_239 nsi_239 ON o.code_vc_nsi_239 = nsi_239.code "
                        + "WHERE o.is_deleted = 0 "
                        + " AND o.uuid_sr_ctr     = :NEW.uuid "
                        + " AND (o.endsupplydate > :NEW.completiondate) "
                    + ") LOOP"
                        + " raise_application_error (-20000, "
                        + "'Некорректная дата окончания договора ресурсоснабжения. Коммунальная услуга ' || i.nsi_239_label "
                        + "|| ' (коммунальный ресурс ' || i.nsi_3_label || ') поставляется с ' "
                        + "|| TO_CHAR (i.startsupplydate, 'DD.MM.YYYY') "
                        + "|| CASE WHEN i.endsupplydate IS NULL THEN NULL ELSE ' по ' "
                        + "|| TO_CHAR (i.endsupplydate, 'DD.MM.YYYY') END "
                        + "|| '. Операция отменена.'); "
                    + " END LOOP; "

                + " END IF; "
        + "END;");
    }

    public enum Action {

	PLACING      (VocGisStatus.i.PENDING_RP_PLACING,   VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_PLACING),
	EDITING      (VocGisStatus.i.PENDING_RP_EDIT,      VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_STATE),
	ANNULMENT    (VocGisStatus.i.PENDING_RP_ANNULMENT, VocGisStatus.i.ANNUL,    VocGisStatus.i.FAILED_ANNULMENT);

	VocGisStatus.i nextStatus;
	VocGisStatus.i okStatus;
	VocGisStatus.i failStatus;

	private Action(VocGisStatus.i nextStatus, VocGisStatus.i okStatus, VocGisStatus.i failStatus) {
	    this.nextStatus = nextStatus;
	    this.okStatus = okStatus;
	    this.failStatus = failStatus;
	}

	public VocGisStatus.i getNextStatus() {
	    return nextStatus;
	}

	public VocGisStatus.i getFailStatus() {
	    return failStatus;
	}

	public VocGisStatus.i getOkStatus() {
	    return okStatus;
	}

	public static Action forStatus(VocGisStatus.i status) {

	    switch (status) {

	    case PENDING_RQ_PLACING:
		return PLACING;
	    case PENDING_RQ_EDIT:
		return EDITING;
	    case PENDING_RQ_ANNULMENT:
		return ANNULMENT;

	    case PENDING_RP_PLACING:
		return PLACING;
	    case PENDING_RP_EDIT:
		return EDITING;
	    case PENDING_RP_ANNULMENT:
		return ANNULMENT;

	    default:
		return null;
	    }
	}
    };
}