package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocChargeInfoType;
import ru.eludia.products.mosgis.db.model.voc.VocConsumptionVolumeDeterminingMethod;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class ChargeInfo extends EnTable {
    
    public static final String TABLE_NAME = "tb_charge_info";
    
    public enum c implements EnColEnum {
        
        UUID_PAY_DOC          (PaymentDocument.class,                           "Платёжный документ"),
        ID_TYPE               (VocChargeInfoType.class,                         "Тип услуги"),
        
        UUID_ORG              (VocOrganization.class,                           "Организация-исполнитель услуги"),
        UUID_BNK_ACCT         (BankAccount.class, null,                         "Платёжные реквизиты"),

        CODE_VC_NSI_50        (Type.STRING,  20,    null,                       "Вид жилищной услуги (НСИ 50)"),
        UUID_M_M_SERVICE      (MainMunicipalService.class, null,                "Коммунальная услуга"),
        UUID_ADD_SERVICE      (AdditionalService.class, null,                   "Дополнительная услуга"),
        UUID_GEN_NEED_RES     (GeneralNeedsMunicipalResource.class, null,       "Коммунальный ресурс, потребляемый при использовании и содержании общего имущества в многоквартирном доме (НСИ 337)"),
        UUID_INS_PRODUCT      (InsuranceProduct.class, null,                    "Страховой продукт"),

        RATE                  (Type.NUMERIC, 14, 6, null,   "Тариф"),
        TOTALPAYABLE          (Type.NUMERIC, 13, 2, null,   "Итого к оплате за расчетный период, руб."),
        ACCOUNTINGPERIODTOTAL (Type.NUMERIC, 13, 2, null,   "Всего начислено за расчетный период (без перерасчетов и льгот), руб."),
        
        CALCEXPLANATION       (Type.STRING,  null,          "Порядок расчетов"),

        CONS_I_VOL            (Type.NUMERIC, 22, 7, null,   "Потреблённый объём услуги / индивидульное потребление"),
        CONS_I_DTRM_METH      (VocConsumptionVolumeDeterminingMethod.class, "Способ определения объёма / индивидульное потребление"),
        
        CONS_O_VOL            (Type.NUMERIC, 22, 7, null,   "Потреблённый объём услуги / общедомовые нужды"),
        CONS_O_DTRM_METH      (VocConsumptionVolumeDeterminingMethod.class, "Способ определения объёма / общедомовые нужды"),

//      SUM                   (Type.NUMERIC, 12, 2, null,   "Сумма перерасчётов, руб."),
//      RECALCULATIONREASON   (Type.STRING,  null,          "Основания перерасчётов"),

        MONEYRECALCULATION    (Type.NUMERIC, 13, 2, null,   "Перерасчеты, корректировки, руб."),
        MONEYDISCOUNT         (Type.NUMERIC, 13, 2, null,   "Льготы, субсидии, скидки, руб."),

        RATIO                 (Type.NUMERIC, 5,  2, null,   "Размер повышающего коэффициента"),
        AMOUNTOFEXCESSFEES    (Type.NUMERIC, 13, 2, null,   "Размер превышения платы, рассчитанной с применением повышающего коэффициента над размером платы, рассчитанной без учета повышающего коэффициента, руб."),
                
        PP_SUM                (Type.NUMERIC, 13, 2, null,   "Сумма к оплате с учётом рассрочки платежа и процентов за рассрочку, руб. (piecemealPaymentSum)"),
        PP_PP_SUM             (Type.NUMERIC, 13, 2, null,   "Сумма платы с учётом рассрочки платежа - от платы за расчётный период, руб. (paymentPeriodPiecemealPaymentSum)"),
        PP_PPP_SUM            (Type.NUMERIC, 13, 2, null,   "Сумма платы с учётом рассрочки платежа - от платы за предыдущие расчётные периоды, руб. (pastPaymentPeriodPiecemealPaymentSum)"),
        PP_RATE_RUB           (Type.NUMERIC, 13, 2, null,   "Проценты за рассрочку, руб. (piecemealPaymentPercentRub)"),
        PP_RATE_PRC           (Type.NUMERIC,  5, 2, null,   "Проценты за рассрочку, %. (piecemealPaymentPercent)"),        

        ID_LOG                (ChargeInfoLog.class,         "Последнее событие редактирования"),

        ;
        
        @Override
        public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        
        @Override
        public boolean isLoggable () {
            switch (this) {
                case ID_LOG:
                case UUID_ORG:
                case UUID_PAY_DOC:
                    return false;
                default:
                    return true;
            }
        }        
        
    }

    public ChargeInfo () {
        
        super  (TABLE_NAME, "Начисления по услугам (строки платёжных документов)");

        cols   (c.class);        

        key (c.UUID_PAY_DOC);
        
        trigger ("BEFORE INSERT OR UPDATE", ""
                  
            + "BEGIN "
                
            + " :NEW.PP_SUM := NVL(:NEW.PP_PP_SUM, 0) + NVL(:NEW.PP_PPP_SUM, 0) + :NEW.PP_RATE_RUB; "
                
            + "END;"

        );        
        
    }    
    
}
