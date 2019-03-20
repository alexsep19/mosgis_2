package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.AttachTable;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.voc.VocFileStatus;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocLegalActLevel;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi237;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi324;

public class LegalAct extends AttachTable  {

    public static final String TABLE_NAME = "tb_legal_acts";

    public enum c implements EnColEnum {
	UUID_ORG               (VocOrganization.class, "Организация, которая завела данный НПА в БД"),

	CODE_VC_NSI_324        (Nsi324.class, "Вид НПА (НСИ 324)"),
	NAME                   (Type.STRING, 1000, null, "Наименование документа"),
	DOCNUMBER              (Type.STRING, 1000, null, "Номер документа"),

	LEVEL_                 (VocLegalActLevel.class, null, "Уровень (сфера действия)"),
	CODE_VC_NSI_237        (Nsi237.class, 20, new ru.eludia.base.model.def.String("77"), "Сфера действия НПА региональный уровень (НСИ 237)"),
	SCOPE                  (Type.BOOLEAN, Bool.FALSE, "1, если действует в заданных ОКТМО, 0, если действует по всей Москве"),

	APPROVEDATE            (Type.DATE, null, "Дата принятия документа органом власти"),
	ID_CTR_STATUS          (VocGisStatus.class,    VocGisStatus.DEFAULT,    "Статус с точки зрения mosgis"),
	ID_CTR_STATUS_GIS      (VocGisStatus.class,    VocGisStatus.DEFAULT,    "Статус с точки зрения ГИС ЖКХ"),

	DOCUMENTGUID           (Type.UUID, null, "Идентификатор НПА в ГИС ЖКХ"),

	ID_LOG                 (LegalActLog.class, "Последнее событие редактирования")
        ;

        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        @Override
        public boolean isLoggable () {
            switch (this) {
                case UUID_ORG:
                case ID_LOG:
                    return false;
                default:
                    return true;
            }
        }

    }

    public LegalAct () {
        super  (TABLE_NAME, "Нормативно-правовые акты (НПА)");
        cols   (c.class);
        key    (c.UUID_ORG);

	key    ("attachmentguid", AttachTable.c.ATTACHMENTGUID);

        trigger ("BEFORE UPDATE", ""
	    + "DECLARE"
	    + " cnt NUMBER; "
	    + "BEGIN "
            + CHECK_LEN

            + "IF :NEW.ID_CTR_STATUS = " + VocGisStatus.i.ANNUL + " THEN " + " :NEW.ID_STATUS := " + VocFileStatus.i.DELETED + "; END IF; "

	    + "IF :NEW.ID_CTR_STATUS <> :OLD.ID_CTR_STATUS AND :NEW.ID_CTR_STATUS=" + VocGisStatus.i.PENDING_RQ_PLACING + " THEN "
	    + "  SELECT COUNT(*) INTO cnt FROM tb_legal_act_oktmo WHERE uuid=:NEW.uuid; "
	    + "  IF :NEW.scope = 1 THEN "
	    + "     IF cnt=0 THEN raise_application_error (-20000, 'Укажите ОКТМО'); END IF; "
	    + "   END IF; "
	    + "  IF :NEW.level_ = " + VocLegalActLevel.i.MUNICIPAL.getId() + " AND cnt <> 1 THEN "
	    + "    raise_application_error (-20000, 'Укажите одно ОКТМО'); "
	    + "  END IF; "
	    + "END IF; "
        + "END;");
    }
    
    public enum Action {
        
        PLACING     (VocGisStatus.i.PENDING_RP_PLACING,   VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_PLACING),
        EDITING     (VocGisStatus.i.PENDING_RP_EDIT,      VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_STATE),
        ANNULMENT   (VocGisStatus.i.PENDING_RP_ANNULMENT, VocGisStatus.i.ANNUL,    VocGisStatus.i.FAILED_ANNULMENT),
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