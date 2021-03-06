package ru.eludia.products.mosgis.db.model.tables;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocChargeInfoType;
import ru.eludia.products.mosgis.db.model.voc.VocConsumptionVolumeDeterminingMethod;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi50;
import ru.gosuslugi.dom.schema.integration.bills.GeneralMunicipalResourceType;
import ru.gosuslugi.dom.schema.integration.bills.PDServiceChargeType;
import ru.gosuslugi.dom.schema.integration.bills.PaymentDocumentType;
import ru.gosuslugi.dom.schema.integration.bills.ServiceChargeImportType;

public class ChargeInfo extends EnTable {
    
    public static final String TABLE_NAME = "tb_charge_info";
    public static final String __GENERAL = "__general";
    
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

        OKEI                  (VocOkei.class, "Единицы измерения (ОКЕИ)"),        

        RATE                  (Type.NUMERIC, 14, 6, null,   "Тариф"),
        TOTALPAYABLE          (Type.NUMERIC, 13, 2, null,   "Итого к оплате за расчетный период, руб."),
        
        ACCOUNTINGPERIODTOTAL (Type.NUMERIC, 13, 2, null,   "Всего начислено за расчетный период (без перерасчетов и льгот), руб."),
        
        CALCEXPLANATION       (Type.STRING,  null,          "Порядок расчетов"),

        CONS_I_VOL            (Type.NUMERIC, 22, 7, null,   "Потреблённый объём услуги / индивидульное потребление"),
        CONS_I_DTRM_METH      (VocConsumptionVolumeDeterminingMethod.class, "Способ определения объёма / индивидульное потребление"),
        
        CONS_O_VOL            (Type.NUMERIC, 22, 7, null,   "Потреблённый объём услуги / общедомовые нужды"),
        CONS_O_DTRM_METH      (VocConsumptionVolumeDeterminingMethod.class, "Способ определения объёма / общедомовые нужды"),

//        SUM                   (Type.NUMERIC, 12, 2, null,   "Сумма перерасчётов, руб."),
        RECALCULATIONREASON   (Type.STRING,  null,          "Основания перерасчётов"),

        MONEYRECALCULATION    (Type.NUMERIC, 13, 2, null,   "Перерасчеты, корректировки, руб."),
        MONEYDISCOUNT         (Type.NUMERIC, 13, 2, null,   "Льготы, субсидии, скидки, руб."),

        LIMITINDEX            (Type.NUMERIC, 6,  2, null,   "Предельный (максимальный) индекс изменения размера платы граждан за коммунальные услуги в муниципальном образовании, %"),
        
        RATIO                 (Type.NUMERIC, 5,  2, null,   "Размер повышающего коэффициента"),
        AMOUNTOFEXCESSFEES    (Type.NUMERIC, 13, 2, null,   "Размер превышения платы, рассчитанной с применением повышающего коэффициента над размером платы, рассчитанной без учета повышающего коэффициента, руб."),
                
        PP_SUM                (Type.NUMERIC, 13, 2, null,   "Сумма к оплате с учётом рассрочки платежа и процентов за рассрочку, руб. (piecemealPaymentSum)"),
        PP_PP_SUM             (Type.NUMERIC, 13, 2, null,   "Сумма платы с учётом рассрочки платежа - от платы за расчётный период, руб. (paymentPeriodPiecemealPaymentSum)"),
        PP_PPP_SUM            (Type.NUMERIC, 13, 2, null,   "Сумма платы с учётом рассрочки платежа - от платы за предыдущие расчётные периоды, руб. (pastPaymentPeriodPiecemealPaymentSum)"),
        PP_RATE_RUB           (Type.NUMERIC, 13, 2, null,   "Проценты за рассрочку, руб. (piecemealPaymentPercentRub)"),
        PP_RATE_PRC           (Type.NUMERIC,  5, 2, null,   "Проценты за рассрочку, %. (piecemealPaymentPercent)"),        

        SI_VAL_IND            (Type.NUMERIC, 22, 7, null,   "Текущие показания приборов учёта коммунальных услуг - индивидуальное потребление (individualConsumptionCurrentValue)"),
        SI_VAL_OVER           (Type.NUMERIC, 22, 7, null,   "Текущие показания приборов учёта коммунальных услуг - общедомовые нужды (houseOverallNeedsCurrentValue)"),

        SI_HT_IND             (Type.NUMERIC, 22, 7, null,   "Суммарный объём коммунальных услуг в доме - индивидуальное потребление (houseTotalIndividualConsumption)"),
        SI_HT_OVER            (Type.NUMERIC, 22, 7, null,   "Суммарный объём коммунальных услуг в доме - общедомовые нужды (houseTotalHouseOverallNeeds)"),

        SI_IND_NORM           (Type.NUMERIC, 22, 7, null,   "Норматив потребления коммунальных услуг - индивидуальное потребление (individualConsumptionNorm)"),
        SI_HO_NORM            (Type.NUMERIC, 22, 7, null,   "Норматив потребления коммунальных услуг - общедомовые нужды (houseOverallNeedsNorm)"),

        AMOUNT_ACK            (Type.NUMERIC, 13, 2, BigDecimal.ZERO,                                          "Сквитировано, руб."),
        AMOUNT_NACK           (Type.NUMERIC, 13, 2, new Virt  ("\"TOTALPAYABLE\"-\"AMOUNT_ACK\""), "Не сквитировано, руб."),
        
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
        
        trigger ("BEFORE INSERT", ""
                  
            + "BEGIN "
                
            + " IF :NEW.CODE_VC_NSI_50 IS NOT NULL THEN "
            + "   SELECT MIN(okei) INTO :NEW.okei FROM " + Nsi50.TABLE_NAME + " WHERE id=:NEW.CODE_VC_NSI_50;"
            + " END IF; "
                
            + " IF :NEW.UUID_M_M_SERVICE IS NOT NULL THEN "
            + "   SELECT MIN(okei) INTO :NEW.okei FROM " + MainMunicipalService.TABLE_NAME + " WHERE uuid=:NEW.UUID_M_M_SERVICE;"
            + " END IF; "
                    
            + " IF :NEW.UUID_ADD_SERVICE IS NOT NULL THEN "
            + "   SELECT MIN(okei) INTO :NEW.okei FROM " + AdditionalService.TABLE_NAME + " WHERE uuid=:NEW.UUID_M_M_SERVICE;"
            + " END IF; "

            + " IF :NEW.UUID_GEN_NEED_RES IS NOT NULL THEN "
            + "   SELECT MIN(okei) INTO :NEW.okei FROM " + GeneralNeedsMunicipalResource.TABLE_NAME + " WHERE uuid=:NEW.UUID_GEN_NEED_RES;"
            + " END IF; "
                    
            + "END;"

        );        
        
        trigger ("BEFORE INSERT OR UPDATE", ""

            + "BEGIN "
            + " :NEW.PP_SUM := NVL(:NEW.PP_PP_SUM, 0) + NVL(:NEW.PP_PPP_SUM, 0) + :NEW.PP_RATE_RUB; "
            + "END;"

        );        
        
    }    
            
    static PaymentDocumentType.ChargeInfo toChargeInfo (Map<String, Object> r) {
        
        final PaymentDocumentType.ChargeInfo result = DB.to.javaBean (PaymentDocumentType.ChargeInfo.class, r);
        
        final VocChargeInfoType.i type = VocChargeInfoType.i.forId (r.get (c.ID_TYPE.lc ()));
        
        switch (type) {
            case HOUSING:
                result.setHousingService (toHousingService (r));
                break;
            case MUNICIPAL:
                result.setMunicipalService (toMunicipalService (r));
                break;
            case ADDITIONAL:
                result.setAdditionalService (toAdditionalService (r));
                break;
            default: 
                return null;
        }        
        
        return result;
        
    }
    
    private static PDServiceChargeType.MunicipalService toMunicipalService (Map<String, Object> r) {
        final PDServiceChargeType.MunicipalService result = DB.to.javaBean (PDServiceChargeType.MunicipalService.class, r);
        result.setServiceType (NsiTable.toDom (r, "nsi"));
        result.setConsumption (toMConsumption (r));
        result.setServiceCharge (toServiceCharge (r));
        result.setPaymentRecalculation (toMPaymentRecalculation (r));
        return result;
    }

    private static PDServiceChargeType.AdditionalService toAdditionalService (Map<String, Object> r) {
        final PDServiceChargeType.AdditionalService result = DB.to.javaBean (PDServiceChargeType.AdditionalService.class, r);
        result.setServiceType (NsiTable.toDom (r, "nsi"));
        result.setConsumption (toConsumption (r));
        result.setServiceCharge (toServiceCharge (r));
        result.setPaymentRecalculation (toPaymentRecalculation (r));
        return result;
    }
    
    private static PDServiceChargeType.HousingService toHousingService (Map<String, Object> r) {
        final PDServiceChargeType.HousingService result = DB.to.javaBean (PDServiceChargeType.HousingService.class, r);
        result.setServiceType (NsiTable.toDom (r, "nsi"));
        addMunicipalResources (result.getMunicipalResource (), (List <Map <String, Object>>) r.get (__GENERAL));
        return result;
    }
    
    private static void addMunicipalResources (List<PDServiceChargeType.HousingService.MunicipalResource> municipalResource, List<Map<String, Object>> list) {
        list.forEach ((t) -> municipalResource.add (toMunicipalResource (t)));
    }
    
    private static PDServiceChargeType.HousingService.MunicipalResource toMunicipalResource (Map<String, Object> r) {
        final PDServiceChargeType.HousingService.MunicipalResource result = DB.to.javaBean (PDServiceChargeType.HousingService.MunicipalResource.class, r);
        result.setServiceType (NsiTable.toDom (r, "vc_nsi_2"));
        result.getGeneralMunicipalResource ().add (toGeneralMunicipalResourceType (r));
        result.setServiceCharge (toServiceCharge (r));
        result.setPaymentRecalculation (toRPaymentRecalculation (r));
        result.setConsumption (toRConsumption (r));
        return result;
    }
    
    private static GeneralMunicipalResourceType toGeneralMunicipalResourceType (Map<String, Object> r) {
        final GeneralMunicipalResourceType result = DB.to.javaBean (GeneralMunicipalResourceType.class, r);
        result.setServiceType (NsiTable.toDom (r, "nsi"));
        result.setServiceCharge (toServiceCharge (r));
        result.setPaymentRecalculation (toGPaymentRecalculation (r));
        result.setConsumption (toGConsumption (r));
        return result;
    }
        
    private static PDServiceChargeType.AdditionalService.Consumption toConsumption (Map<String, Object> r) {
        final PDServiceChargeType.AdditionalService.Consumption result = new PDServiceChargeType.AdditionalService.Consumption ();
        List<PDServiceChargeType.AdditionalService.Consumption.Volume> volume = result.getVolume ();
        addVolume (volume, r, 'i');
        addVolume (volume, r, 'o');
        return volume.isEmpty () ? null : result;
    }
    
    private static PDServiceChargeType.MunicipalService.Consumption toMConsumption (Map<String, Object> r) {
        final PDServiceChargeType.MunicipalService.Consumption result = new PDServiceChargeType.MunicipalService.Consumption ();
        List<PDServiceChargeType.MunicipalService.Consumption.Volume> volume = result.getVolume ();
        addMVolume (volume, r, 'i');
        addMVolume (volume, r, 'o');
        return volume.isEmpty () ? null : result;
    }
    
    private static GeneralMunicipalResourceType.Consumption toGConsumption (Map<String, Object> r) {
        Object vol = r.get ("cons_o_vol");
        if (!DB.ok (vol)) return null;        
        final GeneralMunicipalResourceType.Consumption result = new GeneralMunicipalResourceType.Consumption ();
        GeneralMunicipalResourceType.Consumption.Volume v = new GeneralMunicipalResourceType.Consumption.Volume ();
        v.setValue ((BigDecimal) vol);
        v.setDeterminingMethod (DB.to.String (r.get ("cons_o_dtrm_meth")));
        result.setVolume (v);
        return result;
    }
    
    private static PDServiceChargeType.HousingService.MunicipalResource.Consumption toRConsumption (Map<String, Object> r) {
        Object vol = r.get ("cons_o_vol");
        if (!DB.ok (vol)) return null;
        final PDServiceChargeType.HousingService.MunicipalResource.Consumption result = new PDServiceChargeType.HousingService.MunicipalResource.Consumption ();
        PDServiceChargeType.HousingService.MunicipalResource.Consumption.Volume v = new PDServiceChargeType.HousingService.MunicipalResource.Consumption.Volume ();
        v.setValue ((BigDecimal) vol);
        v.setDeterminingMethod (DB.to.String (r.get ("cons_o_dtrm_meth")));
        result.setVolume (v);
        return result;
    }

    private static void addVolume (List<PDServiceChargeType.AdditionalService.Consumption.Volume> volume, Map<String, Object> r, char c) {
        Object vol = r.get ("cons_" + c + "_vol");
        if (!DB.ok (vol)) return;
        final PDServiceChargeType.AdditionalService.Consumption.Volume v = new PDServiceChargeType.AdditionalService.Consumption.Volume ();
        v.setValue ((BigDecimal) vol);
        v.setType (("" + c).toUpperCase ());
        volume.add (v);
    }
    
    private static void addMVolume (List<PDServiceChargeType.MunicipalService.Consumption.Volume> volume, Map<String, Object> r, char c) {
        Object vol = r.get ("cons_" + c + "_vol");
        if (!DB.ok (vol)) return;
        final PDServiceChargeType.MunicipalService.Consumption.Volume v = new PDServiceChargeType.MunicipalService.Consumption.Volume ();
        v.setValue ((BigDecimal) vol);
        v.setType (("" + c).toUpperCase ());
        v.setDeterminingMethod (DB.to.String (r.get ("cons_" + c + "_dtrm_meth")));
        volume.add (v);
    }

    private static PDServiceChargeType.AdditionalService.PaymentRecalculation toPaymentRecalculation (Map<String, Object> r) {
        Object sum = r.get (c.MONEYRECALCULATION.lc ());
        if (!DB.ok (sum)) return null;
        final PDServiceChargeType.AdditionalService.PaymentRecalculation result = new PDServiceChargeType.AdditionalService.PaymentRecalculation ();
        result.setSum ((BigDecimal) sum);
        result.setRecalculationReason (DB.to.String (r.get (c.RECALCULATIONREASON.lc ())));
        return result;
    }
    
    private static PDServiceChargeType.MunicipalService.PaymentRecalculation toMPaymentRecalculation (Map<String, Object> r) {
        Object sum = r.get (c.MONEYRECALCULATION.lc ());
        if (!DB.ok (sum)) return null;
        final PDServiceChargeType.MunicipalService.PaymentRecalculation result = new PDServiceChargeType.MunicipalService.PaymentRecalculation ();
        result.setSum ((BigDecimal) sum);
        result.setRecalculationReason (DB.to.String (r.get (c.RECALCULATIONREASON.lc ())));
        return result;
    }    
    
    private static GeneralMunicipalResourceType.PaymentRecalculation toGPaymentRecalculation (Map<String, Object> r) {
        Object sum = r.get (c.MONEYRECALCULATION.lc ());
        if (!DB.ok (sum)) return null;
        final GeneralMunicipalResourceType.PaymentRecalculation result = new GeneralMunicipalResourceType.PaymentRecalculation ();
        result.setSum ((BigDecimal) sum);
        result.setRecalculationReason (DB.to.String (r.get (c.RECALCULATIONREASON.lc ())));
        return result;
    }
    
    private static PDServiceChargeType.HousingService.MunicipalResource.PaymentRecalculation toRPaymentRecalculation (Map<String, Object> r) {
        Object sum = r.get (c.MONEYRECALCULATION.lc ());
        if (!DB.ok (sum)) return null;
        final PDServiceChargeType.HousingService.MunicipalResource.PaymentRecalculation result = new PDServiceChargeType.HousingService.MunicipalResource.PaymentRecalculation ();
        result.setSum ((BigDecimal) sum);
        result.setRecalculationReason (DB.to.String (r.get (c.RECALCULATIONREASON.lc ())));
        return result;
    }
    
    private static ServiceChargeImportType toServiceCharge (Map<String, Object> r) {
        Object sum = r.get (c.MONEYDISCOUNT.lc ());
        if (!DB.ok (sum)) return null;
        final ServiceChargeImportType result = new ServiceChargeImportType ();
        result.setMoneyDiscount ((BigDecimal) sum);
        return result;
    }
    
    static PaymentDocumentType.Insurance toInsurance (Map<String, Object> r) {
        final PaymentDocumentType.Insurance result = DB.to.javaBean (PaymentDocumentType.Insurance.class, r);
        return result;
    }

}