package ru.eludia.products.mosgis.db.model.tables;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.BOOLEAN;
import static ru.eludia.base.model.Type.DATE;
import static ru.eludia.base.model.Type.NUMERIC;
import static ru.eludia.base.model.Type.STRING;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocContractPaymentType;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.gosuslugi.dom.schema.integration.house_management.ImportContractRequest;

public class ContractPayment extends EnTable {

    public enum c implements EnColEnum {

        UUID_CONTRACT        (Contract.class,          "Ссылка на договор"),
        UUID_CONTRACT_OBJECT (ContractObject.class,    "Ссылка на объект договора"),
        UUID_VOTING_PROTOCOL (VotingProtocol.class,    "Ссылка на протокол ОСС"),
        UUID_FILE            (ContractPaymentFile.class, "Ссылка на протокол вложение"),
        FIASHOUSEGUID        (VocBuilding.class,       "Дом"),
        ID_CTR_STATUS        (VocGisStatus.class,      VocGisStatus.DEFAULT, "Статус объекта договора с точки зрения mosgis"),
        ID_CTR_STATUS_GIS    (VocGisStatus.class,      VocGisStatus.DEFAULT, "Статус объекта договора с точки зрения ГИС ЖКХ"),        
        TYPE_                (VocContractPaymentType.class,    "Тип размера платы"),
        IS_PROTO             (BOOLEAN,                 new Virt ("DECODE(\"TYPE_\", 'P', 1, 0)"),  "1, если основание — протокол ОСС, иначе 0"),
        
        ID_LOG               (ContractPaymentLog.class,  "Последнее событие редактирования"),
        
        BEGINDATE            (DATE,                    "Дата начала периода"),
        ENDDATE              (DATE,                    "Дата окончания периода"),
        HOUSEMANAGEMENTPAYMENTSIZE (NUMERIC, 10, 2, null,  "Размер платы (цена) за услуги, работы по управлению МКД (если утверждена протоколом обшего собрания собственников)/Размер платы за содержание жилого помещения, установленный по результатам открытого конкурса (если утверждена протоколом открытого конкурса)"),

        REASON               (STRING, 1000, null,    "Причина аннулирования"),
        IS_ANNULED           (BOOLEAN,      new Virt ("DECODE(\"REASON\",NULL,0,1)"),  "1, если запись аннулирована; иначе 0")

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
                case IS_PROTO:
                case UUID_CONTRACT:
                case UUID_CONTRACT_OBJECT:
                case FIASHOUSEGUID:
                    return false;
                default: 
                    return true;
            }
        }

    }

    public ContractPayment () {
        
        super ("tb_ctr_payments", "[Сведения о размере платы за] услуги управления");

        cols   (c.class);
        
        key    ("uuid_contract", c.UUID_CONTRACT);
        key    ("uuid_contract_object", c.UUID_CONTRACT_OBJECT);

        trigger ("BEFORE INSERT",                 
            "BEGIN "
            + "  IF :NEW.uuid_contract_object IS NOT NULL THEN "
                + "SELECT fiashouseguid INTO :NEW.fiashouseguid FROM tb_contract_objects WHERE uuid=:NEW.uuid_contract_object; "                      
            + "  END IF;"
            + "END;"                
        );
                
        trigger ("BEFORE INSERT OR UPDATE", ""
                
            + "DECLARE" 
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
            + "BEGIN "                    
                    
            + "IF :NEW.is_deleted = 0 THEN "

                + " FOR i IN ("
                    + "SELECT "
                    + " o.begindate"
                    + " , o.enddate "
                    + "FROM "
                    + " tb_ctr_payments o "
                    + "WHERE o.is_deleted = 0"
                    + " AND o.uuid_contract = :NEW.uuid_contract "
                    + " AND NVL (o.uuid_contract_object, '00') = NVL (:NEW.uuid_contract_object, '00') "
                    + " AND o.enddate   >= :NEW.begindate "
                    + " AND o.begindate <= :NEW.enddate "
                    + " AND o.uuid <> NVL(:NEW.uuid, '00') "
                    + ") LOOP"
                + " raise_application_error (-20000, "
                    + "'Указанный период пересекается с другой информацией о размере платы за жилое помещение с ' "
                    + "|| TO_CHAR (i.begindate, 'DD.MM.YYYY')"
                    + "||' по '"
                    + "|| TO_CHAR (i.enddate, 'DD.MM.YYYY')"
                    + "|| '. Операция отменена.'); "
                + " END LOOP; "

            + "END IF; "                                        
                    
        + "END;");        

    }
    
    private static final Logger logger = Logger.getLogger (ContractPayment.class.getName ());    
    
    public enum Action {
        
        PLACING     (VocGisStatus.i.PENDING_RP_PLACING,   VocGisStatus.i.FAILED_PLACING),
        ANNULMENT   (VocGisStatus.i.PENDING_RP_ANNULMENT, VocGisStatus.i.FAILED_ANNULMENT)
//        EDITING     (VocGisStatus.i.PENDING_RP_EDIT,      VocGisStatus.i.FAILED_STATE),
//        TERMINATION (VocGisStatus.i.PENDING_RP_TERMINATE, VocGisStatus.i.FAILED_TERMINATE),
//        ROLLOVER    (VocGisStatus.i.PENDING_RP_ROLLOVER,  VocGisStatus.i.FAILED_STATE),
//        RELOADING   (VocGisStatus.i.PENDING_RP_RELOAD,    VocGisStatus.i.FAILED_STATE)
//        APPROVING   (VocGisStatus.i.PENDING_RP_APPROVAL,  VocGisStatus.i.FAILED_STATE),
//        REFRESHING  (VocGisStatus.i.PENDING_RP_REFRESH,   VocGisStatus.i.FAILED_STATE),
        ;
        
        VocGisStatus.i nextStatus;
        VocGisStatus.i failStatus;

        private Action (VocGisStatus.i nextStatus, VocGisStatus.i failStatus) {
            this.nextStatus = nextStatus;
            this.failStatus = failStatus;
        }

        public VocGisStatus.i getNextStatus () {
            return nextStatus;
        }

        public VocGisStatus.i getFailStatus () {
            return failStatus;
        }
        
        public static Action forStatus (VocGisStatus.i status) {
            switch (status) {
                case PENDING_RQ_PLACING:   return PLACING;
                case PENDING_RQ_ANNULMENT: return ANNULMENT;
//                case PENDING_RQ_EDIT:      return EDITING;
//                case PENDING_RQ_TERMINATE: return TERMINATION;
//                case PENDING_RQ_RELOAD:    return RELOADING;
//                case PENDING_RQ_ROLLOVER:  return ROLLOVER;
//                case PENDING_RQ_APPROVAL:  return APPROVING;
//                case PENDING_RQ_REFRESH:   return REFRESHING;
                default: return null;
            }            
        }
                        
    };
    
    public static final ImportContractRequest.Contract.AnnulmentContractPaymentsInfo toAnnulmentContractPaymentsInfo (Map<String, Object> r) {
        final ImportContractRequest.Contract.AnnulmentContractPaymentsInfo ac = (ImportContractRequest.Contract.AnnulmentContractPaymentsInfo) DB.to.javaBean (ImportContractRequest.Contract.AnnulmentContractPaymentsInfo.class, r);
        return ac;
    }
    
    public static final ImportContractRequest.Contract.PlaceContractPaymentsInfo toPlaceContractPaymentsInfo (Map<String, Object> r) {
        r.put ("type", r.get ("type_"));
logger.info ("r=" + r);
        final ImportContractRequest.Contract.PlaceContractPaymentsInfo pc = (ImportContractRequest.Contract.PlaceContractPaymentsInfo) DB.to.javaBean (ImportContractRequest.Contract.PlaceContractPaymentsInfo.class, r);        
        pc.setContractVersionGUID (r.get ("ctrt.contractversionguid").toString ());
        
        final Object contractobjectversionguid = r.get ("o.contractobjectversionguid");
        if (DB.ok (contractobjectversionguid)) pc.setContractObjectVersionGUID (contractobjectversionguid.toString ());
        
        for (Map <String, Object> i: (List <Map <String, Object>>) r.get ("svc")) pc.getServicePayment ().add (ServicePayment.toServicePayment (i));
        
        if (DB.ok (r.get ("doc.attachmentguid"))) {
            pc.getProtocol ().add (AttachTable.toAttachmentType (
                r.get ("doc.label"), 
                r.get ("doc.description"), 
                r.get ("doc.attachmentguid"), 
                r.get ("doc.attachmenthash")
            ));
        }
        
        return pc;
        
    }

}