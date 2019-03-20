package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPaymentDocumentType;

public class PaymentDocument extends EnTable {

    public static final String TABLE_NAME = "tb_pay_docs";

    public enum c implements EnColEnum {
        
        UUID_ORG                      (VocOrganization.class, null, "Организация, которая создала данный платёжный документ"),
        UUID_ACCOUNT                  (Account.class,               "Лицевой счёт"),

        ID_TYPE                       (VocPaymentDocumentType.class, VocPaymentDocumentType.DEFAULT,  "Тип ЛС"),

        YEAR                          (Type.NUMERIC, 4,             "Год"),
        MONTH                         (Type.NUMERIC, 4,             "Месяц"),
        
        PAYMENTDOCUMENTNUMBER         (Type.STRING,  30,    null,   "Номер платежного документа, по которому внесена плата, присвоенный такому документу исполнителем в целях осуществления расчетов по внесению платы"),
        
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
        
        ID_CTR_STATUS                 (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения mosgis"),
        ID_CTR_STATUS_GIS             (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения ГИС ЖКХ"),
        
        ;

        @Override
        public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        
        @Override
        public boolean isLoggable () {
            switch (this) {
//                case ID_LOG:
                case UUID_ORG:
                case UUID_ACCOUNT:
                    return false;
                default:
                    return true;
            }
        }        

    }

    public PaymentDocument () {
        
        super  (TABLE_NAME, "Лицевые счета");
        
        cols   (c.class);        
        
        key (c.UUID_ACCOUNT);
               
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