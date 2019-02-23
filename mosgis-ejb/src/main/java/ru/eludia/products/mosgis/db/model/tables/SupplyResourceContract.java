package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.voc.nsi.VocNsi239;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.eludia.products.mosgis.db.model.voc.VocGisContractDimension;
import ru.eludia.products.mosgis.db.model.voc.VocGisSupplyResourceContractCustomerType;
import ru.eludia.products.mosgis.db.model.voc.VocSupplyResourceContractFileType;


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
	TERMINATE            (Type.DATE, null, "Дата расторжения"),
	CODE_VC_NSI_54       (Type.STRING, 20, null, "Ссылка на НСИ \"Основание расторжения договора\" (реестровый номер 54)"),
        AUTOROLLOVER         (Type.BOOLEAN, Bool.FALSE, "1, если автопролонгация на год; иначе 0"),

	LABEL                (Type.STRING, new Virt("'№' || contractnumber || ' от ' || TO_CHAR (signingdate, 'DD.MM.YYYY')"), "№/дата"),

        CODE_VC_NSI_58       (Type.STRING, 20, "Основание заключения договора (НСИ 58)"),
        IS_CONTRACT          (Type.BOOLEAN,    null, "1, если является публичным; иначе 0"),

        ONETIMEPAYMENT       (Type.BOOLEAN, null, "1, если единоразовая оплата услуг; иначе 0"),
        COUNTINGRESOURCE     (Type.BOOLEAN, null, "1, если РСО размещает информацию о начислениях за коммунальные услуги; 0 - размещает Исполнитель коммунальных услуг."),
        ISPLANNEDVOLUME      (Type.BOOLEAN, null, "1, если в договоре есть плановый объем и режим подачи поставки ресурсов; иначе 0"),

        PLANNEDVOLUMETYPE    (VocGisContractDimension.class
		, null
                , "Тип ведения планового объема и режима подачи в разрезе"
        ),
        ACCRUALPROCEDURE     (VocGisContractDimension.class
		, null
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
                + "DECLARE"
                + " cnt NUMBER; "
                + "BEGIN "

                + " IF :NEW.is_deleted = 0 THEN "

		    + " IF :NEW.isplannedvolume IS NULL THEN "
		    + "     :NEW.isplannedvolume:= 0; "
		    + " END IF; "

		    + " IF :NEW.isplannedvolume = 0 THEN "
		    + "   :NEW.plannedvolumetype := NULL; "
		    + " END IF; "

		    + " IF :NEW.isplannedvolume = 1 AND :NEW.plannedvolumetype IS NULL THEN "
		    + "   raise_application_error (-20000, 'Укажите тип ведения планового объема и режима подачи. Операция отменена.'); "
		    + " END IF; "

		    + " IF :NEW.id_customer_type = " + VocGisSupplyResourceContractCustomerType.i.ORGANIZATION.getId() + " THEN "
		    + "   :NEW.onetimepayment:= NULL; "
		    + "   :NEW.volumedepends:= NULL; "
		    + "   IF :NEW.uuid_org_customer IS NULL THEN "
		    + "     raise_application_error (-20000, 'Укажите организацию-заказчик. Операция отменена.'); "
		    + "   END IF; "
		    + " ELSE "
		    + "   :NEW.accrualprocedure:= NULL; "
		    + "   :NEW.countingresource:= NULL; "
		    + "   :NEW.mdinfo:= NULL; "
		    + " END IF; "

		    + " IF :NEW.onetimepayment = 1 THEN "
		    + "   :NEW.volumedepends:= NULL; "
		    + "   :NEW.ddt_d_start:= NULL; "
		    + "   :NEW.ddt_d_start_nxt:= NULL; "
		    + "   :NEW.ddt_i_start:= NULL; "
		    + "   :NEW.ddt_i_start_nxt:= NULL; "
		    + " END IF; "

		    + " IF :NEW.accrualprocedure <> " + VocGisContractDimension.i.BY_CONTRACT.getId() + " THEN "
		    + "   :NEW.countingresource:= NULL; "
		    + " END IF; "

		    + " IF :NEW.countingresource <> 1 THEN "
		    + "   :NEW.mdinfo:= NULL; "
		    + " END IF; "

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

		    + " IF :NEW.ID_CTR_STATUS <> :OLD.ID_CTR_STATUS AND :NEW.ID_CTR_STATUS=" + VocGisStatus.i.PENDING_RQ_PLACING + " THEN "

			+ " IF :NEW.is_contract = 1 THEN "
			+ "   SELECT COUNT(*) INTO cnt FROM tb_sr_ctr_files WHERE is_deleted = 0 AND id_type=" + VocSupplyResourceContractFileType.i.CONTRACT + " AND id_status=1 AND UUID_SR_CTR=:NEW.uuid; "
			+ "   IF cnt=0 THEN raise_application_error (-20000, 'Файл договора не загружен на сервер. Операция отменена.'); END IF; "
			+ "   FOR i IN ("
			+ "     SELECT "
			+ "       o.uuid "
			+ "     FROM "
			+ "       tb_sr_ctr_subj o "
			+ "     INNER JOIN vc_nsi_239 ON vc_nsi_239.code = o.code_vc_nsi_239 AND vc_nsi_239.isactual = 1"
			+ "     INNER JOIN vc_nsi_276 ON vc_nsi_276.guid_vc_nsi_239 = vc_nsi_239.guid AND vc_nsi_276.isactual = 1"
			+ "     LEFT JOIN tb_sr_ctr_qls ql ON ql.uuid_sr_ctr_subj = o.uuid "
			+ "       AND ql.is_deleted = 0 "
			+ "       AND ql.code_vc_nsi_276 = vc_nsi_276.code "
			+ "     WHERE o.is_deleted = 0 "
			+ "       AND o.uuid_sr_ctr     = :NEW.uuid "
			+ "       AND :NEW.specqtyinds  = " + VocGisContractDimension.i.BY_CONTRACT
			+ "       AND o.uuid_sr_ctr_obj IS NULL "
			+ "       AND o.code_vc_nsi_239 IN ("
			+ VocNsi239.CODE_DRINK_WATER + "," + VocNsi239.CODE_HOT_WATER + "," + VocNsi239.CODE_HEAT_ENERGY
			+ "       )"
			+ "       AND ql.uuid IS NULL "
			+ "      ) LOOP "
			+ "        raise_application_error (-20000, "
			+ "          'Для коммунальных ресурсов \"Питьевая вода\", \"Горячая вода\" и \"Тепловая энергия\" в предмете договора должны быть заполнены все показатели качества коммунального ресурса из справочника ГИС ЖКХ номер 236' "
			+ "          || '. Операция отменена.'); "
			+ "   END LOOP; "

			+ "   FOR i IN ("
			+ "     SELECT "
			+ "       o.uuid "
			+ "     FROM "
			+ "       tb_sr_ctr_subj o "
			+ "     INNER JOIN vc_nsi_239 ON vc_nsi_239.code = o.code_vc_nsi_239 AND vc_nsi_239.isactual = 1"
			+ "     INNER JOIN vc_nsi_276 ON vc_nsi_276.guid_vc_nsi_239 = vc_nsi_239.guid AND vc_nsi_276.isactual = 1"
			+ "     LEFT JOIN tb_sr_ctr_qls ql ON ql.uuid_sr_ctr_subj = o.uuid "
			+ "       AND ql.is_deleted = 0 "
			+ "       AND ql.code_vc_nsi_276 = vc_nsi_276.code "
			+ "     WHERE o.is_deleted = 0 "
			+ "       AND o.uuid_sr_ctr     = :NEW.uuid "
			+ "       AND :NEW.specqtyinds  = " + VocGisContractDimension.i.BY_HOUSE
			+ "       AND o.uuid_sr_ctr_obj IS NOT NULL "
			+ "       AND o.code_vc_nsi_239 IN ("
			+ VocNsi239.CODE_DRINK_WATER + "," + VocNsi239.CODE_HOT_WATER + "," + VocNsi239.CODE_HEAT_ENERGY
			+ "       )"
			+ "       AND ql.uuid IS NULL "
			+ "      ) LOOP "
			+ "        raise_application_error (-20000, "
			+ "          'Для коммунальных ресурсов \"Питьевая вода\", \"Горячая вода\" и \"Тепловая энергия\" в каждом коммунальном ресурсе объекта жилищного фонда договора должны быть заполнены все показатели качества коммунального ресурса из справочника ГИС ЖКХ номер 236' "
			+ "          || '. Операция отменена.'); "
			+ "   END LOOP; "

			+ " END IF; " // IF :NEW.is_contract = 1

			+ " SELECT COUNT(*) INTO cnt FROM tb_sr_ctr_subj WHERE is_deleted = 0 AND uuid_sr_ctr_obj IS NULL AND UUID_SR_CTR=:NEW.uuid; "
			+ " IF cnt=0 THEN raise_application_error (-20000, 'Укажите предметы договора. Операция отменена.'); END IF; "

			+ " SELECT COUNT(*) INTO cnt FROM tb_sr_ctr_obj WHERE is_deleted = 0 AND id_ctr_status <> " + VocGisStatus.i.ANNUL + "AND UUID_SR_CTR=:NEW.uuid; "
			+ " IF cnt=0 THEN raise_application_error (-20000, 'Укажите объекты жилищного фонда договора. Операция отменена.'); END IF; "

			+ " SELECT COUNT(*) INTO cnt FROM tb_sr_ctr_subj WHERE is_deleted = 0 AND uuid_sr_ctr_obj IS NOT NULL AND UUID_SR_CTR=:NEW.uuid; "
			+ " IF cnt=0 THEN raise_application_error (-20000, 'Укажите поставляемые ресурсы в каждом объекте жилищного фонда договора. Операция отменена.'); END IF; "

			+ " IF :NEW.id_customer_type = " + VocGisSupplyResourceContractCustomerType.i.ORGANIZATION + " THEN "
			+ "   IF :NEW.accrualprocedure IS NULL THEN "
			+ "     raise_application_error (-20000, 'Необходимо заполнить поле \"Порядок размещения начислений за услуги\". Операция отменена.'); "
			+ "   END IF; "
			+ "   IF :NEW.accrualprocedure = " + VocGisContractDimension.i.BY_CONTRACT + " AND :NEW.countingresource IS NULL THEN "
			+ "     raise_application_error (-20000, 'Необходимо заполнить поле \"Размещает начисления за услуги\". Операция отменена.'); "
			+ "   END IF; "
			+ "   IF :NEW.DDT_N_START IS NULL AND :NEW.countingresource = 1 THEN raise_application_error (-20000, 'Необходимо заполнить поля \"Срок предоставления информации о поступивших платежах\". Операция отменена.'); END IF; "
			+ " ELSE "
			+ "   IF (:NEW.volumedepends = 1 OR :NEW.mdinfo = 1) AND (:NEW.DDT_M_START IS NULL OR :NEW.DDT_M_END IS NULL) THEN "
			+ "     raise_application_error (-20000, 'Необходимо заполнить поля \"периода ввода показаний ПУ\". Операция отменена.'); "
			+ "   END IF; "
			+ "   IF :NEW.DDT_I_START IS NULL AND :NEW.onetimepayment <> 1 AND :NEW.is_contract <> 1 THEN "
			+ "     raise_application_error (-20000, 'Необходимо заполнить поля \"Срок внесения платы\", если заказчик - не исполнитель коммунальных услуг. Операция отменена.'); "
			+ "   END IF; "
			+ " END IF; " // IF :NEW.id_customer_type = " + VocGisSupplyResourceContractCustomerType.i.ORGANIZATION

			+ " IF :NEW.DDT_D_START IS NULL AND :NEW.id_customer_type <> " + VocGisSupplyResourceContractCustomerType.i.ORGANIZATION + " THEN "
			+ "   raise_application_error (-20000, 'Необходимо заполнить поля \"Срок выставления платежных документов\". Операция отменена.'); "
			+ " END IF; "

			+ " IF :NEW.DDT_D_START IS NULL AND :NEW.MDINFO = 1 THEN "
			+ "   raise_application_error (-20000, 'Необходимо заполнить поля \"Срок выставления платежных документов\". Операция отменена.'); "
			+ " END IF; "

			+ " IF :NEW.id_customer_type = " + VocGisSupplyResourceContractCustomerType.i.OWNER + " THEN "
			+ "   FOR i IN ("
			+ "     SELECT "
			+ "       b.label "
			+ "     FROM "
			+ "       tb_sr_ctr_obj o "
			+ "     INNER JOIN vc_build_addresses b ON b.houseguid = o.fiashouseguid "
			+ "     WHERE o.is_deleted = 0 "
			+ "       AND o.uuid_sr_ctr  = :NEW.uuid "
			+ "       AND b.is_condo = 0 "
			+ "      ) LOOP "
			+ "        raise_application_error (-20000, "
			+ "          'Вторая сторона договора Собственник (пользователь) помещений МКД, а объект жилищного фонда ' || i.label || ' ЖД' "
			+ "          || '. Операция отменена.'); "
			+ "   END LOOP; "
			+ " END IF; "

			+ " IF :NEW.id_customer_type = " + VocGisSupplyResourceContractCustomerType.i.LIVINGHOUSEOWNER + " THEN "
			+ "   FOR i IN ("
			+ "     SELECT "
			+ "       b.label "
			+ "     FROM "
			+ "       tb_sr_ctr_obj o "
			+ "     INNER JOIN vc_build_addresses b ON b.houseguid = o.fiashouseguid "
			+ "     WHERE o.is_deleted = 0 "
			+ "       AND o.uuid_sr_ctr  = :NEW.uuid "
			+ "       AND b.is_condo = 1 "
			+ "      ) LOOP "
			+ "        raise_application_error (-20000, "
			+ "          'Вторая сторона договора Собственник (пользователь) помещений ЖД, а объект жилищного фонда ' || i.label || ' МКД' "
			+ "          || '. Операция отменена.'); "
			+ "   END LOOP; "
			+ " END IF; "

			+ " FOR i IN ("
			+ "SELECT "
			+ " o.uuid "
			+ "FROM "
			+ " tb_sr_ctr_subj o "
			+ "INNER JOIN tb_sr_ctr_obj obj ON obj.uuid = o.uuid_sr_ctr_obj "
			+ " AND obj.is_deleted = 0 "
			+ " AND obj.id_ctr_status <> " + VocGisStatus.i.ANNUL
			+ "WHERE o.is_deleted = 0 "
			+ " AND o.uuid_sr_ctr     = :NEW.uuid "
			+ " AND o.code_vc_nsi_239 IN ("
			+ VocNsi239.CODE_HEAT_ENERGY + "," + VocNsi239.CODE_HOT_WATER
			+ " )"
			+ " AND o.is_heat_open IS NULL "
			+ ") LOOP "
			+ " raise_application_error (-20000, "
			+ "'Для коммунальных ресурсов \"Тепловая энергия\" и \"Горячая вода\" должен быть указан тип системы теплоснабжения для каждого объекта жилищного фонда в договоре' "
			+ "|| '. Операция отменена.'); "
			+ " END LOOP; "

			+ " IF :NEW.plannedvolumetype = " + VocGisContractDimension.i.BY_CONTRACT + " THEN "
			    + " FOR i IN ("
			    + "SELECT "
			    + " o.uuid "
			    + "FROM "
			    + " tb_sr_ctr_subj o "
			    + "WHERE o.is_deleted = 0 "
			    + " AND o.uuid_sr_ctr_obj IS NULL "
			    + " AND o.uuid_sr_ctr     = :NEW.uuid "
			    + " AND o.volume IS NULL "
			    + ") LOOP "
			    + " raise_application_error (-20000, "
			    + "'В каждом предмете договора должен быть указан плановый объем и режим подачи' "
			    + "|| '. Операция отменена.'); "
			    + " END LOOP; "
			+ " END IF; "
			+ " IF :NEW.plannedvolumetype = " + VocGisContractDimension.i.BY_HOUSE + " THEN "
			    + " FOR i IN ("
			    + "SELECT "
			    + " o.uuid "
			    + "FROM "
			    + " tb_sr_ctr_subj o "
			    + "WHERE o.is_deleted = 0 "
			    + " AND o.uuid_sr_ctr_obj IS NOT NULL "
			    + " AND o.uuid_sr_ctr     = :NEW.uuid "
			    + " AND o.volume IS NULL "
			    + ") LOOP "
			    + " raise_application_error (-20000, "
			    + "'В каждом поставляемом ресурсе объекта жилищного фонда договора должен быть указан плановый объем и режим подачи' "
			    + "|| '. Операция отменена.'); "
			    + " END LOOP; "
			+ " END IF; "
		    + " END IF; " // IF :NEW.ID_CTR_STATUS=" + VocGisStatus.i.PENDING_RQ_PLACING

		    + " IF :NEW.ID_CTR_STATUS <> :OLD.ID_CTR_STATUS AND :NEW.ID_CTR_STATUS=" + VocGisStatus.i.TERMINATED + " THEN "
		    + "   UPDATE tb_sr_ctr_subj o "
		    + "     SET o.endsupplydate = :NEW.terminate "
		    + "   WHERE o.is_deleted = 0 "
		    + "     AND o.uuid_sr_ctr = :NEW.uuid; "
		    + " END IF; " // IF :NEW.ID_CTR_STATUS=" + VocGisStatus.i.TERMINATED

		    + " IF :NEW.ID_CTR_STATUS <> :OLD.ID_CTR_STATUS THEN "
		    + "   UPDATE tb_sr_ctr_obj o "
		    + "     SET o.id_ctr_status = :NEW.ID_CTR_STATUS "
		    + "   WHERE o.is_deleted = 0 "
		    + "     AND o.id_ctr_status <>  " + VocGisStatus.i.ANNUL
		    + "     AND o.uuid_sr_ctr = :NEW.uuid; "
		    + " END IF; " // IF :NEW.ID_CTR_STATUS <> :OLD.ID_CTR_STATUS

	    + " END IF; " // IF :NEW.is_deleted = 0
        + "END;");
    }

    public enum Action {

	PLACING      (VocGisStatus.i.PENDING_RQ_PLACING,   VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_PLACING),
	EDITING      (VocGisStatus.i.PENDING_RQ_EDIT,      VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_STATE),
	TERMINATION  (VocGisStatus.i.PENDING_RQ_TERMINATE, VocGisStatus.i.TERMINATED, VocGisStatus.i.FAILED_TERMINATE),
	ANNULMENT    (VocGisStatus.i.PENDING_RQ_ANNULMENT, VocGisStatus.i.ANNUL,    VocGisStatus.i.FAILED_ANNULMENT);

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

	    case PENDING_RQ_PLACING: return PLACING;
	    case PENDING_RQ_EDIT: return EDITING;
	    case PENDING_RQ_ANNULMENT: return ANNULMENT;
	    case PENDING_RQ_TERMINATE: return TERMINATION;

	    default:
		return null;
	    }
	}
    };
}