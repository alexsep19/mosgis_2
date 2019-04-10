package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.def.Bool.FALSE;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocAccountType;
import ru.eludia.products.mosgis.db.model.voc.VocChargeInfoType;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPaymentDocumentType;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi2;

public class PaymentDocument extends EnTable {

    public static final String TABLE_NAME = "tb_pay_docs";

    public enum c implements EnColEnum {
        
        UUID_ORG                      (VocOrganization.class, null, "Организация, которая создала данный платёжный документ"),
        UUID_ACCOUNT                  (Account.class,               "Лицевой счёт"),

        ID_TYPE                       (VocPaymentDocumentType.class, VocPaymentDocumentType.DEFAULT,  "Тип ЛС"),

        YEAR                          (Type.NUMERIC, 4,             "Год"),
        MONTH                         (Type.NUMERIC, 4,             "Месяц"),
        DT_PERIOD                     (Type.DATE,           null,   "ГГГГ-ММ-01"),
        
        PAYMENTDOCUMENTNUMBER         (Type.STRING,  30,    null,   "Номер платежного документа, по которому внесена плата, присвоенный такому документу исполнителем в целях осуществления расчетов по внесению платы"),
        PAYMENTDOCUMENTID             (Type.STRING,  18,    null,   "Идентификатор платежного документа"),

        ADDITIONALINFORMATION         (Type.STRING,  4000,  null,   "Дополнительная информация"),
        
        DEBTPREVIOUSPERIODS           (Type.NUMERIC, 10, 2, null,   "Задолженность за предыдущие периоды, руб."), 
        ADVANCEBLLINGPERIOD           (Type.NUMERIC, 10, 2, null,   "Аванс на начало расчетного периода, руб."), 
        
        PAYMENTSTAKEN_DT              (Type.DATE,           null,   "Дата последнего учтенного платежа"),
        PAYMENTSTAKEN                 (Type.NUMERIC,  2, 0, new Virt  ("EXTRACT(DAY FROM\"PAYMENTSTAKEN_DT\")"),   "Учтены платежи, поступившие до указанного числа расчетного периода включительно"),
        
        TOTALPAYABLEBYCHARGEINFO      (Type.NUMERIC, 13, 2, null,   "Сумма к оплате за расчетный период по услугам, руб."),        
        TOTALBYPENALTIESANDCOURTCOSTS (Type.NUMERIC, 13, 2, null,   "Итого к оплате по неустойкам и судебным издержкам, руб."),        
        TOTALPAYABLEBYPD              (Type.NUMERIC, 13, 2, null,   "Итого к оплате за расчетный период всего, руб."),        
        SUBSIDIESCOMPENSATION_        (Type.NUMERIC, 10, 2, null,   "Субсидии, компенсации и иные меры соц. поддержки граждан, руб."),               
        TOTALPAYABLEBYPDWITH_DA       (Type.NUMERIC, 13, 2, null,   "Итого к оплате за расчетный период c учетом задолженности/переплаты, руб."),
        PAIDCASH                      (Type.NUMERIC, 13, 2, null,   "Оплачено денежных средств, руб."),

        DATEOFLASTRECEIVEDPAYMENT     (Type.DATE,           null,   "Дата последней поступившей оплаты"),
        LIMITINDEX                    (Type.NUMERIC,  5, 2, null,   "Предельный (максимальный) индекс изменения размера платы граждан за коммунальные услуги в муниципальном образовании, %"),
        
        IS_POWER_SUPPLY               (Type.BOOLEAN,      FALSE,  "1, если это документ РСО, поставляющей электроэнергию; иначе 0"),
        
        UUID_BNK_ACCT                 (BankAccount.class,            "Платёжные реквизиты (для ПД в целом, по умолчанию)"),

        ID_CTR_STATUS                 (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения mosgis"),
        ID_CTR_STATUS_GIS             (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения ГИС ЖКХ"),
        
        ID_LOG                        (PaymentDocumentLog.class, "Последнее событие редактирования"),        
        
        ;

        @Override
        public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        
        @Override
        public boolean isLoggable () {
            switch (this) {
                case ID_LOG:
                case UUID_ORG:
                case UUID_ACCOUNT:
                    return false;
                default:
                    return true;
            }
        }        

    }

    public PaymentDocument () {

        super  (TABLE_NAME, "Платёжные документы");

        cols   (c.class);        

        key (c.UUID_ACCOUNT);
	key (c.UUID_ORG);

        trigger ("BEFORE INSERT OR UPDATE", ""

            + "BEGIN "
            + "  IF :NEW.TOTALPAYABLEBYPDWITH_DA IS NULL THEN "
            + "    :NEW.TOTALPAYABLEBYPDWITH_DA := NVL (:NEW.TOTALPAYABLEBYPD, 0) - NVL (:NEW.ADVANCEBLLINGPERIOD, 0) + NVL (:NEW.DEBTPREVIOUSPERIODS, 0);"
            + "  END IF;"                
            + "END;"

        );

        trigger ("BEFORE INSERT", ""

            + "DECLARE" 
            + "  PRAGMA AUTONOMOUS_TRANSACTION; "
            + "  l_acct " + Account.TABLE_NAME + " %ROWTYPE; "                
            + " BEGIN "

            + " :NEW.dt_period := TO_DATE (:NEW.year || LPAD (:NEW.month, 2, '0') || '01', 'YYYYMMDD'); "
                    
            + "  SELECT * INTO l_acct FROM " + Account.TABLE_NAME + " WHERE uuid=:NEW.UUID_ACCOUNT;"

            + " IF l_acct.UUID_CONTRACT IS NOT NULL THEN "
                + " SELECT MIN (uuid_bnk_acct) INTO :NEW.uuid_bnk_acct FROM " + Contract.TABLE_NAME + " WHERE uuid=l_acct.UUID_CONTRACT;"
            + " END IF; "                                        

            + " IF l_acct.UUID_CHARTER IS NOT NULL THEN "
                + " SELECT MIN (uuid_bnk_acct) INTO :NEW.uuid_bnk_acct FROM " + Charter.TABLE_NAME + " WHERE uuid=l_acct.UUID_CHARTER;"
            + " END IF; "                                        

            + " IF l_acct.UUID_SR_CONTRACT IS NOT NULL THEN "
                + " SELECT MIN (uuid_bnk_acct) INTO :NEW.uuid_bnk_acct FROM " + SupplyResourceContract.TABLE_NAME + " WHERE uuid=l_acct.UUID_SR_CONTRACT;"
            + " END IF; "                                        

            + " IF l_acct.UUID_RC_CONTRACT IS NOT NULL THEN "
                + " SELECT MIN (uuid_bnk_acct) INTO :NEW.uuid_bnk_acct FROM " + RcContract.TABLE_NAME + " WHERE uuid=l_acct.UUID_RC_CONTRACT;"
            + " END IF; "                    

            + " IF :NEW.uuid_bnk_acct IS NULL THEN "
                + " raise_application_error (-20000, 'Для договора-основания лицевого счёта не заданы платёжные реквизиты. Операция отменена.'); "
            + " END IF; "                    
                        
            + "  IF l_acct.id_type = " + VocAccountType.i.RSO + " THEN "                    
                + " FOR i IN ("
                    + "SELECT "
                    + " * "
                    + "FROM "
                    + ActualBuildingMainMunicipalServices.TABLE_NAME + " t "
                    + " INNER JOIN " + MainMunicipalService.TABLE_NAME + " s ON t." + ActualBuildingMainMunicipalServices.c.UUID_M_M_SERVICE + " = s.uuid "
                    + "WHERE "
                    + " t.FIASHOUSEGUID=l_acct.fiashouseguid "
                    + " AND t.uuid_org=l_acct.uuid_org "
                    + " AND s.code_vc_nsi_2 = '" + Nsi2.i.POWER.getCode () + "'"
                    + ") LOOP"
                + " :NEW.IS_POWER_SUPPLY := 1; "
                + " END LOOP; "
            + " END IF; "

            + "END; "

        );                
        
        trigger ("AFTER INSERT", ""

            + " DECLARE" 
            + "  l_uuid_ins_product RAW (16);" 
            + "  l_acct " + Account.TABLE_NAME + " %ROWTYPE; "                
            + "  cnt NUMBER;" 
            + " BEGIN "

            + "  SELECT * INTO l_acct FROM " + Account.TABLE_NAME + " WHERE uuid=:NEW.UUID_ACCOUNT;"

//////// CR                    
                    
            + "  IF l_acct.id_type = " + VocAccountType.i.CR + " THEN BEGIN "

                + "  INSERT INTO " + ChargeInfo.TABLE_NAME + " (UUID_PAY_DOC, UUID_ORG, ID_TYPE, UUID_BNK_ACCT) VALUES (:NEW.UUID, :NEW.UUID_ORG, " + VocChargeInfoType.i.OVERHAUL + ", :NEW.uuid_bnk_acct);"

            + "  END; END IF; "                                        

//////// RSO

            + "  IF l_acct.id_type = " + VocAccountType.i.RSO + " THEN BEGIN "
                    
                + "  INSERT INTO " + ChargeInfo.TABLE_NAME + " (UUID_PAY_DOC, UUID_ORG, ID_TYPE, UUID_BNK_ACCT, UUID_M_M_SERVICE) SELECT :NEW.UUID UUID_PAY_DOC, t.UUID_ORG, " + VocChargeInfoType.i.MUNICIPAL + " ID_TYPE, :NEW.uuid_bnk_acct, t.UUID_M_M_SERVICE"
                    + " FROM  " + ActualBuildingMainMunicipalServices.TABLE_NAME + " t "
                    + " WHERE FIASHOUSEGUID=l_acct.fiashouseguid AND uuid_org=l_acct.uuid_org;"

                + "  INSERT INTO " + ChargeInfo.TABLE_NAME + " (UUID_PAY_DOC, UUID_ORG, ID_TYPE, UUID_BNK_ACCT, UUID_ADD_SERVICE) SELECT :NEW.UUID UUID_PAY_DOC, t.UUID_ORG, " + VocChargeInfoType.i.ADDITIONAL + " ID_TYPE, :NEW.uuid_bnk_acct, t.UUID_ADD_SERVICE"
                    + " FROM  " + ActualBuildingAdditionalServices.TABLE_NAME + " t "
                    + " WHERE FIASHOUSEGUID=l_acct.fiashouseguid AND uuid_org=l_acct.uuid_org;"

                + "  INSERT INTO " + ChargeInfo.TABLE_NAME + " (UUID_PAY_DOC, UUID_ORG, ID_TYPE, UUID_BNK_ACCT, UUID_ADD_SERVICE) SELECT :NEW.UUID UUID_PAY_DOC, l_acct.UUID_ORG, " + VocChargeInfoType.i.ADDITIONAL + " ID_TYPE, :NEW.uuid_bnk_acct, t.UUID_ADD_SERVICE"
                    + " FROM  " + AccountIndividualService.TABLE_NAME + " t "
                    + " WHERE uuid_account=l_acct.uuid AND :NEW.DT_PERIOD BETWEEN begindate AND enddate;"

            + "  END; END IF; "         

//////// UO
                    
            + "  IF l_acct.id_type = " + VocAccountType.i.UO + " THEN BEGIN "

                + "  INSERT INTO " + ChargeInfo.TABLE_NAME + " (UUID_PAY_DOC, UUID_ORG, ID_TYPE, UUID_BNK_ACCT, CODE_VC_NSI_50) VALUES (:NEW.UUID, :NEW.UUID_ORG, " + VocChargeInfoType.i.HOUSING + ", :NEW.uuid_bnk_acct, 1);"

                + "  INSERT INTO " + ChargeInfo.TABLE_NAME + " (UUID_PAY_DOC, UUID_ORG, ID_TYPE, UUID_BNK_ACCT, UUID_GEN_NEED_RES) SELECT :NEW.UUID UUID_PAY_DOC, t.UUID_ORG, " + VocChargeInfoType.i.GENERAL + " ID_TYPE, :NEW.uuid_bnk_acct, t.UUID"
                    + " FROM  " + ActualBuildingGeneralNeedsMunicipalResource.TABLE_NAME + " t "
                    + " WHERE FIASHOUSEGUID=l_acct.fiashouseguid AND uuid_org=l_acct.uuid_org;"

                + "  INSERT INTO " + ChargeInfo.TABLE_NAME + " (UUID_PAY_DOC, UUID_ORG, ID_TYPE, UUID_BNK_ACCT, UUID_M_M_SERVICE) SELECT :NEW.UUID UUID_PAY_DOC, t.UUID_ORG, " + VocChargeInfoType.i.MUNICIPAL + " ID_TYPE, :NEW.uuid_bnk_acct, t.UUID_M_M_SERVICE"
                    + " FROM  " + ActualBuildingMainMunicipalServices.TABLE_NAME + " t "
                    + " WHERE FIASHOUSEGUID=l_acct.fiashouseguid AND uuid_org=l_acct.uuid_org;"

                + "  INSERT INTO " + ChargeInfo.TABLE_NAME + " (UUID_PAY_DOC, UUID_ORG, ID_TYPE, UUID_BNK_ACCT, UUID_ADD_SERVICE) SELECT :NEW.UUID UUID_PAY_DOC, t.UUID_ORG, " + VocChargeInfoType.i.ADDITIONAL + " ID_TYPE, :NEW.uuid_bnk_acct, t.UUID_ADD_SERVICE"
                    + " FROM  " + ActualBuildingAdditionalServices.TABLE_NAME + " t "
                    + " WHERE FIASHOUSEGUID=l_acct.fiashouseguid AND uuid_org=l_acct.uuid_org;"
                            
                + "  INSERT INTO " + ChargeInfo.TABLE_NAME + " (UUID_PAY_DOC, UUID_ORG, ID_TYPE, UUID_BNK_ACCT, UUID_ADD_SERVICE) SELECT :NEW.UUID UUID_PAY_DOC, l_acct.UUID_ORG, " + VocChargeInfoType.i.ADDITIONAL + " ID_TYPE, :NEW.uuid_bnk_acct, t.UUID_ADD_SERVICE"
                    + " FROM  " + AccountIndividualService.TABLE_NAME + " t "
                    + " WHERE uuid_account=l_acct.uuid AND :NEW.DT_PERIOD BETWEEN begindate AND enddate;"

                + "  SELECT MIN(uuid), COUNT(*) cnt INTO l_uuid_ins_product, cnt FROM " + InsuranceProduct.TABLE_NAME + " WHERE uuid_org=l_acct.uuid_org;"
                // + "  IF cnt <> 1 THEN :NEW.uuid_bnk_acct := NULL; END IF;"

                + "  IF l_uuid_ins_product IS NOT NULL THEN "
                + "    INSERT INTO " + ChargeInfo.TABLE_NAME + " (UUID_PAY_DOC, UUID_ORG, ID_TYPE, UUID_BNK_ACCT, UUID_INS_PRODUCT) VALUES (:NEW.UUID, :NEW.UUID_ORG, " + VocChargeInfoType.i.INSURANCE + ", :NEW.uuid_bnk_acct, l_uuid_ins_product);"
                + "  END IF; "

            + "  END; END IF; "                        
                        
//////// RC
                    
            + "  IF l_acct.id_type = " + VocAccountType.i.RC + " THEN BEGIN "

                + "  INSERT INTO " + ChargeInfo.TABLE_NAME + " (UUID_PAY_DOC, UUID_ORG, ID_TYPE, UUID_BNK_ACCT, CODE_VC_NSI_50) VALUES (:NEW.UUID, :NEW.UUID_ORG, " + VocChargeInfoType.i.HOUSING + ", :NEW.uuid_bnk_acct, 1);"

                + "  INSERT INTO " + ChargeInfo.TABLE_NAME + " (UUID_PAY_DOC, UUID_ORG, ID_TYPE, UUID_BNK_ACCT, UUID_GEN_NEED_RES) SELECT :NEW.UUID UUID_PAY_DOC, t.UUID_ORG, " + VocChargeInfoType.i.GENERAL + " ID_TYPE, :NEW.uuid_bnk_acct, t.UUID"
                    + " FROM  " + ActualBuildingGeneralNeedsMunicipalResource.TABLE_NAME + " t "
                    + " WHERE FIASHOUSEGUID=l_acct.fiashouseguid AND uuid_org IN (SELECT uuid_org_customer FROM vw_rc_address_map WHERE uuid_org=l_acct.uuid_org AND (fiashouseguid IS NULL OR fiashouseguid=l_acct.fiashouseguid) AND (dt_from IS NULL OR dt_from <= :NEW.DT_PERIOD) AND (dt_to IS NULL OR dt_to >= :NEW.DT_PERIOD) );"

                + "  INSERT INTO " + ChargeInfo.TABLE_NAME + " (UUID_PAY_DOC, UUID_ORG, ID_TYPE, UUID_BNK_ACCT, UUID_M_M_SERVICE) SELECT :NEW.UUID UUID_PAY_DOC, t.UUID_ORG, " + VocChargeInfoType.i.MUNICIPAL + " ID_TYPE, :NEW.uuid_bnk_acct, t.UUID_M_M_SERVICE"
                    + " FROM  " + ActualBuildingMainMunicipalServices.TABLE_NAME + " t "
                    + " WHERE FIASHOUSEGUID=l_acct.fiashouseguid AND uuid_org IN (SELECT uuid_org_customer FROM vw_rc_address_map WHERE uuid_org=l_acct.uuid_org AND (fiashouseguid IS NULL OR fiashouseguid=l_acct.fiashouseguid) AND (dt_from IS NULL OR dt_from <= :NEW.DT_PERIOD) AND (dt_to IS NULL OR dt_to >= :NEW.DT_PERIOD) );"

                + "  INSERT INTO " + ChargeInfo.TABLE_NAME + " (UUID_PAY_DOC, UUID_ORG, ID_TYPE, UUID_BNK_ACCT, UUID_ADD_SERVICE) SELECT :NEW.UUID UUID_PAY_DOC, t.UUID_ORG, " + VocChargeInfoType.i.ADDITIONAL + " ID_TYPE, :NEW.uuid_bnk_acct, t.UUID_ADD_SERVICE"
                    + " FROM  " + ActualBuildingAdditionalServices.TABLE_NAME + " t "
                    + " WHERE FIASHOUSEGUID=l_acct.fiashouseguid AND uuid_org IN (SELECT uuid_org_customer FROM vw_rc_address_map WHERE uuid_org=l_acct.uuid_org AND (fiashouseguid IS NULL OR fiashouseguid=l_acct.fiashouseguid) AND (dt_from IS NULL OR dt_from <= :NEW.DT_PERIOD) AND (dt_to IS NULL OR dt_to >= :NEW.DT_PERIOD) );"

                + "  INSERT INTO " + ChargeInfo.TABLE_NAME + " (UUID_PAY_DOC, UUID_ORG, ID_TYPE, UUID_BNK_ACCT, UUID_ADD_SERVICE) SELECT :NEW.UUID UUID_PAY_DOC, l_acct.UUID_ORG, " + VocChargeInfoType.i.ADDITIONAL + " ID_TYPE, :NEW.uuid_bnk_acct, t.UUID_ADD_SERVICE"
                    + " FROM  " + AccountIndividualService.TABLE_NAME + " t "
                    + " WHERE uuid_account=l_acct.uuid AND :NEW.DT_PERIOD BETWEEN begindate AND enddate;"

                + "  SELECT MIN(uuid), COUNT(*) cnt INTO l_uuid_ins_product, cnt FROM " + InsuranceProduct.TABLE_NAME + " WHERE uuid_org=l_acct.uuid_org;"
                // + "  IF cnt <> 1 THEN :NEW.uuid_bnk_acct := NULL; END IF;"

                + "  IF l_uuid_ins_product IS NOT NULL THEN "
                + "    INSERT INTO " + ChargeInfo.TABLE_NAME + " (UUID_PAY_DOC, UUID_ORG, ID_TYPE, UUID_BNK_ACCT, UUID_INS_PRODUCT) VALUES (:NEW.UUID, :NEW.UUID_ORG, " + VocChargeInfoType.i.INSURANCE + ", :NEW.uuid_bnk_acct, l_uuid_ins_product);"
                + "  END IF; "

            + "  END; END IF; "                        
                        

            + " END; "

        );                
        

    }
/*
    public enum Action {
        
        PLACING     (VocGisStatus.i.PENDING_RP_PLACING,   VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_PLACING),
        EDITING     (VocGisStatus.i.PENDING_RP_EDIT,      VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_STATE),
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
                case PENDING_RQ_EDIT:      return EDITING;
                case PENDING_RP_PLACING:   return PLACING;
                case PENDING_RP_EDIT:      return EDITING;
                default: return null;
            }            
        }

    };    
*/
}