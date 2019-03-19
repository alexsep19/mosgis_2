package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocAccountType;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPaymentDocumentType;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;

public class PaymentDocument extends EnTable {

    public static final String TABLE_NAME = "tb_pay_docs";

    public enum c implements EnColEnum {
        
        UUID_ORG               (VocOrganization.class, null, "Организация, которая создала данный платёжный документ"),
        UUID_ACCOUNT           (Account.class,               "Лицевой счёт"),

        ID_TYPE                (VocPaymentDocumentType.class, VocPaymentDocumentType.DEFAULT,  "Тип ЛС"),

        YEAR                   (Type.NUMERIC, 4,             "Год"),
        MONTH                  (Type.NUMERIC, 4,             "Месяц"),
        
        PAYMENTDOCUMENTNUMBER  (Type.STRING,  30,    null,   "Номер платежного документа, по которому внесена плата, присвоенный такому документу исполнителем в целях осуществления расчетов по внесению платы"),
        
        

/*        
        
	UUID_CONTRACT          (Contract.class,               null, "Ссылка на договор"),
        UUID_CHARTER           (Charter.class,                null, "Ссылка на устав"),
        UUID_SR_CONTRACT       (SupplyResourceContract.class, null, "Ссылка на договор поставки ресурсов"),

        ACCOUNTNUMBER          (Type.STRING,  30,    null,  "Причина закрытия (НСИ 22)"),
        
        LIVINGPERSONSNUMBER    (Type.NUMERIC, 4,     null,  "Количество проживающих"),
        TOTALSQUARE            (Type.NUMERIC, 25, 4, null,  "Общая площадь жилого помещения"),
        RESIDENTIALSQUARE      (Type.NUMERIC, 25, 4, null,  "Жилая площадь"),
        HEATEDAREA             (Type.NUMERIC, 25, 4, null,  "Отапливаемая площадь"),
        
        CODE_VC_NSI_22         (Type.STRING,  20,    null,                                  "Причина закрытия (НСИ 22)"),
        CLOSEREASON            (Type.STRING,  20,    new Virt  ("''||\"CODE_VC_NSI_22\""),  "Причина закрытия (НСИ 22)"),
        CLOSEDATE              (Type.DATE,           null,                                  "Дата закрытия"),

        DESCRIPTION            (Type.STRING,  250,   null,                                  "Примечание (для закрытия)"),
  
        ISRENTER               (Type.BOOLEAN,          null,  "1, если является нанимателем; 0, если не является нанимателем"),
        ISACCOUNTSDIVIDED      (Type.BOOLEAN,          null,  "1, если лицевые счета на помещение(я) разделены; 0, если лицевые счета на помещение(я) не разделены"),

        IS_CUSTOMER_ORG        (Type.BOOLEAN,          Boolean.FALSE,  "1, если плательщик — юридическое лицо; 0, если физическое"),

        UUID_ORG_CUSTOMER      (VocOrganization.class, null, "Организация. ЮЛ/ИП/ОП"),
        UUID_PERSON_CUSTOMER   (VocPerson.class,       null, "Физическое лицо/индивидуальный предприниматель."),
        
        ACCOUNTGUID            (Type.UUID,             null,    "Идентификатор ЛС в ГИС ЖКХ (при обновлении данных ЛС)"),        

        ID_LOG                 (AccountLog.class,                    "Последнее событие редактирования"),
        
*/        

        ID_CTR_STATUS          (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения mosgis"),
        ID_CTR_STATUS_GIS      (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения ГИС ЖКХ"),
        
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