package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import static ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract.Action.TERMINATION;
import ru.eludia.products.mosgis.db.model.voc.VocBic;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.gosuslugi.dom.schema.integration.bills.ImportPaymentDocumentRequest;

public class BankAccount extends EnTable {

    public static final String TABLE_NAME = "tb_bnk_accts";

    public enum c implements EnColEnum {
        
        UUID_ORG               (VocOrganization.class, null, "Организация-владелец счёта"),
	BIKCREDORG             (VocBic.class,          null, "БИК"),
        
        ACCOUNTNUMBER          (Type.STRING,  20,      null, "Номер счёта"),
        
        OPENDATE               (Type.DATE,             null, "Дата открытия/изменения реквизитов"),
        CLOSEDATE              (Type.DATE,             null, "Дата закрытия"),

	IS_ROKR                (Type.BOOLEAN,  null, "1, если является счетом регионального оператора капитального ремонта, иначе 0"),
	UUID_CRED_ORG          (VocOrganization.class, null, "Кредитная организация счета регионального оператора капитального ремонта"),

	ACCOUNTREGOPERATORGUID (Type.UUID, null, "Идентификатор в ГИС ЖКХ"),
	ID_CTR_STATUS          (VocGisStatus.class,    VocGisStatus.DEFAULT,    "Статус с точки зрения mosgis"),
	ID_CTR_STATUS_GIS      (VocGisStatus.class,    VocGisStatus.DEFAULT,    "Статус с точки зрения ГИС ЖКХ"),

        ID_LOG                 (BankAccountLog.class,  null, "Последнее обытие редактирования"),
        
        ;

        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        
        @Override
        public boolean isLoggable () {            
            switch (this) {
                case ID_LOG:
                    return false;
                default:
                    return true;
            }
        }        

    }

    public BankAccount () {
        
        super  (TABLE_NAME, "Платёжные реквизиты (расчётные счета)");
        
        cols   (c.class);        
        
        key ("uuid_org", "uuid_org");

        trigger ("BEFORE INSERT OR UPDATE", 
                
            "DECLARE "
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
            + " cnt NUMBER;"
            + "BEGIN "

		+ "IF :NEW.ID_CTR_STATUS <> :OLD.ID_CTR_STATUS "
		    + " AND :OLD.ID_CTR_STATUS <> " + VocGisStatus.i.FAILED_PLACING
		    + " AND :NEW.ID_CTR_STATUS =  " + VocGisStatus.i.PROJECT
		+ " THEN "
		    + " :NEW.ID_CTR_STATUS := " + VocGisStatus.i.MUTATING
		+ "; END IF; "
                + "IF :NEW.is_deleted=0 THEN BEGIN "
                    
                    + " FOR i IN ("
                        + "SELECT "
                        + " o.uuid "
                        + " , org.label "
                        + "FROM "
                        + TABLE_NAME + "  o "
                        + " LEFT JOIN " + VocOrganization.TABLE_NAME +  " org ON o.UUID_ORG = org.uuid"
                        + " WHERE o.is_deleted = 0"
			+ " AND o.id_ctr_status <> " + VocGisStatus.i.ANNUL
                        + " AND o.uuid <> :NEW.UUID "
                        + " AND o.ACCOUNTNUMBER = :NEW.ACCOUNTNUMBER "
                        + " AND o.BIKCREDORG = :NEW.BIKCREDORG "
                        + ") LOOP"
                    + " raise_application_error (-20000, 'Номер расчетного счета выбранной кредитной организации уже используется ' || i.label || '. Операция отменена.'); "
                    + " END LOOP; "

		    + "IF :NEW.is_rokr = 1 THEN "
			+ " FOR i IN ("
			    + "SELECT "
			    + " o.uuid "
			    + " , o.opendate "
			    + " , o.closedate "
			    + " , org.label "
			    + "FROM "
			    + TABLE_NAME + "  o "
			    + " LEFT JOIN " + VocOrganization.TABLE_NAME +  " org ON o.UUID_ORG = org.uuid"
			    + " WHERE o.is_deleted = 0"
			    + " AND o.uuid <> :NEW.UUID "
			    + " AND o.is_rokr = 1 "
			    + " AND o.id_ctr_status <> " + VocGisStatus.i.ANNUL
			    + " AND (o.opendate  < :NEW.closedate OR :NEW.closedate IS NULL) "
			    + " AND (o.closedate > :NEW.opendate OR o.closedate IS NULL) "
			    + ") LOOP"
			+ " raise_application_error (-20000, 'Уже есть счет регионального оператора капитального ремонта ' || i.label "
			+ "   || ', действующий с ' "
			+ "   || TO_CHAR (i.opendate, 'DD.MM.YYYY') "
			+ "   || CASE WHEN i.closedate IS NULL THEN NULL ELSE ' по ' || TO_CHAR (i.closedate, 'DD.MM.YYYY') END "
			+ " ); "
			+ " END LOOP; "
		    + " END IF; "

                + "END; END IF; " // IF :NEW.is_deleted=0
                                                                            
                + "IF :OLD.is_deleted=0 AND :NEW.is_deleted=1 THEN BEGIN "
                                
                    + " FOR i IN ("
                        + "SELECT o.* "
                        + "FROM " + Contract.TABLE_NAME + " o "
                        + " WHERE o.is_deleted = 0 AND o.is_annuled = 0 "
                        + " AND :NEW.uuid = o." + Contract.c.UUID_BNK_ACCT
                        + ") LOOP"
                    + " raise_application_error (-20000, 'Данный расчётный счёт указан в качестве платёжного реквизита договора управления №' || i.DOCNUM || ' от ' || TO_CHAR (i.SIGNINGDATE, 'DD.MM.YYYY') || '. Операция отменена.'); "
                    + " END LOOP; "

                    + " FOR i IN ("
                        + "SELECT org.label "
                        + "FROM " + Charter.TABLE_NAME + " o "
                        + " INNER JOIN " + VocOrganization.TABLE_NAME + " org ON o.uuid_org=org.uuid"
                        + " WHERE o.is_deleted = 0 AND o.is_annuled = 0 "
                        + " AND :NEW.uuid = o." + Charter.c.UUID_BNK_ACCT
                        + ") LOOP"
                    + " raise_application_error (-20000, 'Данный расчётный счёт указан в качестве платёжного реквизита устава ' || i.label || '. Операция отменена.'); "
                    + " END LOOP; "

                    + " FOR i IN ("
                        + "SELECT o.* "
                        + "FROM " + RcContract.TABLE_NAME + " o "
                        + " WHERE o.is_deleted = 0 "
                        + " AND :NEW.uuid = o." + RcContract.c.UUID_BNK_ACCT
                        + ") LOOP"
                    + " raise_application_error (-20000, 'Данный расчётный счёт указан в качестве платёжного реквизита договора услуг РЦ №' || i.CONTRACTNUMBER || ' от ' || TO_CHAR (i.SIGNINGDATE, 'DD.MM.YYYY') || '. Операция отменена.'); "
                    + " END LOOP; "

                    + " FOR i IN ("
                        + "SELECT o.* "
                        + "FROM " + SupplyResourceContract.TABLE_NAME + " o "
                        + " WHERE o.is_deleted = 0 AND o.is_annuled = 0 "
                        + " AND :NEW.uuid = o." + SupplyResourceContract.c.UUID_BNK_ACCT
                        + ") LOOP"
                    + " raise_application_error (-20000, 'Данный расчётный счёт указан в качестве платёжного реквизита договора ресурсоснабжения №' || i.CONTRACTNUMBER || ' от ' || TO_CHAR (i.SIGNINGDATE, 'DD.MM.YYYY') || '. Операция отменена.'); "
                    + " END LOOP; "
                                
                + "END; END IF; " // IF :OLD.is_deleted=0 AND :NEW.is_deleted=1


		+ "IF "
		    + "     :OLD.ID_CTR_STATUS = " + VocGisStatus.i.MUTATING
		    + " AND :NEW.ID_CTR_STATUS = " + VocGisStatus.i.PENDING_RQ_PLACING
		+ " THEN "
		    + " :NEW.ID_CTR_STATUS := " + VocGisStatus.i.PENDING_RQ_EDIT
		+ "; END IF; "

            + "END;"

        );

    }

    static ImportPaymentDocumentRequest.PaymentInformation toPaymentInformation (Map<String, Object> r) {
        final ImportPaymentDocumentRequest.PaymentInformation result = new ImportPaymentDocumentRequest.PaymentInformation ();
        result.setBankBIK                (DB.to.String (r.get (c.BIKCREDORG.lc ())));
        result.setOperatingAccountNumber (DB.to.String (r.get (c.ACCOUNTNUMBER.lc ())));
        result.setTransportGUID          (DB.to.String (r.get (EnTable.c.UUID.lc ())));
        return result;
    }

    public enum Action {
        
        PLACING     (VocGisStatus.i.PENDING_RQ_PLACING,   VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_PLACING),
        EDITING     (VocGisStatus.i.PENDING_RQ_EDIT,      VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_STATE),
        ANNULMENT   (VocGisStatus.i.PENDING_RQ_ANNULMENT, VocGisStatus.i.ANNUL,    VocGisStatus.i.FAILED_ANNULMENT),
	TERMINATION  (VocGisStatus.i.PENDING_RQ_TERMINATE, VocGisStatus.i.TERMINATED, VocGisStatus.i.FAILED_TERMINATE),
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
                case PENDING_RQ_ANNULMENT: return ANNULMENT;
		case PENDING_RQ_TERMINATE: return TERMINATION;
                default: return null;
            }
            
        }
    };
}