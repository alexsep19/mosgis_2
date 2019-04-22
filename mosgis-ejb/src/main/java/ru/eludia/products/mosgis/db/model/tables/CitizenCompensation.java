package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocAddressRegistrationType;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;

public class CitizenCompensation extends EnTable {

    public static final String TABLE_NAME = "tb_cit_comps";

    public enum c implements EnColEnum {

	UUID_ORG                     (VocOrganization.class, "Поставщик информации"),

	UUID_PERSON                  (VocPerson.class, "Физическое лицо"),

	FIASHOUSEGUID                (VocBuilding.class, "Адрес (GUID ФИАС)"),
	REGISTRATIONTYPE             (VocAddressRegistrationType.class, "Тип регистрации"),
	APARTMENTNUMBER              (Type.STRING, 36, null, "Номер квартиры"),
	FLATNUMBER                   (Type.STRING, 36, null, "Номер комнаты"),

	ID_LOG                       (CitizenCompensationLog.class, "Последнее событие редактирования"),
	CITIZENCOMPENSATIONGUID      (Type.UUID, null, "Идентификатор в ГИС ЖКХ"),

	ID_CTR_STATUS                (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения mosgis"),
	ID_CTR_STATUS_GIS            (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения ГИС ЖКХ"),
        ;

        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {

            switch (this) {
                case UUID_ORG:
		case UUID_PERSON:
                case ID_LOG:
                    return false;
                default:
                    return true;
            }

        }

    }

    public CitizenCompensation () {

        super (TABLE_NAME, "Граждане, получающие компенсацию расходов");

        cols (c.class);

        key (c.UUID_ORG);
    }

    public enum Action {
        
        PLACING     (VocGisStatus.i.PENDING_RQ_PLACING,   VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_PLACING),
        EDITING     (VocGisStatus.i.PENDING_RQ_EDIT,      VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_STATE),
        ANNULMENT   (VocGisStatus.i.PENDING_RQ_ANNULMENT, VocGisStatus.i.ANNUL,    VocGisStatus.i.FAILED_ANNULMENT),
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
                case PENDING_RP_PLACING:   return PLACING;
                case PENDING_RQ_EDIT:      return EDITING;
                case PENDING_RP_EDIT:      return EDITING;
                case PENDING_RQ_ANNULMENT: return ANNULMENT;
                case PENDING_RP_ANNULMENT: return ANNULMENT;
                default: return null;
            }
            
        }
    };
}