package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class GeneralNeedsMunicipalResource extends EnTable {
    
    public static final String TABLE_NAME = "tb_gen_need_m_res";
    
    public enum c implements EnColEnum {
        
        UUID_ORG                     (VocOrganization.class, null,  "Организация, которая создала данную запись"),

        PARENTCODE                   (Type.NUMERIC, 1,              "Код родительской записи верхнего уровня"),
        
        GENERALMUNICIPALRESOURCENAME (Type.STRING,  1000,    null,  "Наименование главного коммунального ресурса"),
        LABEL_UC                     (Type.STRING,           new Virt  ("UPPER(\"GENERALMUNICIPALRESOURCENAME\")"), "НАИМЕНОВАНИЕ"),
        
        CODE_VC_NSI_2                (Type.STRING,  20,      null,  "Вид коммунального ресурса (НСИ 2)"),

        OKEI                         (VocOkei.class,                "Единицы измерения (ОКЕИ)"),
        SORTORDER                    (Type.STRING,  20,      null,  "Порядок сортировки"),
        SORTORDERNOTDEFINED          (Type.BOOLEAN,          new Virt  ("DECODE(\"SORTORDER\",NULL,1,0)"), "Порядок сортировки не задан"),

        ELEMENTGUID                  (Type.UUID,             null,  "Идентификатор существующего элемента справочника"),
        UNIQUENUMBER                 (Type.STRING,           null,  "Уникальный номер, присвоенный ГИС ЖКХ"),

        GUID                         (Type.UUID,    new Virt ("HEXTORAW(''||RAWTOHEX(\"ELEMENTGUID\"))"),  "GUID НСИ"),
        CODE                         (Type.STRING,  new Virt ("(''||\"UNIQUENUMBER\")"),  "Код НСИ"),
        
        ID_LOG                       (GeneralNeedsMunicipalResourceLog.class, "Последнее событие редактирования"),
        
        ID_CTR_STATUS                (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения mosgis"),
        ID_CTR_STATUS_GIS            (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения ГИС ЖКХ"),        
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

    public GeneralNeedsMunicipalResource () {

        super (TABLE_NAME, "Коммунальные ресурсы, потребляемые при использовании и содержании общего имущества в многоквартирном доме (НСИ 337)");
        
        cols (c.class);
        
        key (c.UUID_ORG);
        
        trigger ("BEFORE INSERT OR UPDATE", ""
            
            + "BEGIN "

                + " IF :NEW.ID_CTR_STATUS <> " + VocGisStatus.i.FAILED_STATE + " AND :NEW.ID_CTR_STATUS_GIS = 10 THEN BEGIN "
                
                    + " IF :NEW.is_deleted=0 THEN :NEW.ID_CTR_STATUS := " + VocGisStatus.i.PENDING_RQ_PLACING
                    + " ; ELSE :NEW.ID_CTR_STATUS := " + VocGisStatus.i.PENDING_RQ_CANCEL
                    + " ; END IF;"
                        
                + " END; END IF; "
                            
                + " :NEW.ID_CTR_STATUS_GIS := 10; "
                        
            + "END;"
                    
        );
        

    }
    
    public enum Action {
        
        PLACING     (VocGisStatus.i.PENDING_RP_PLACING, VocGisStatus.i.APPROVED, VocGisStatus.i.FAILED_PLACING),
        CANCEL      (VocGisStatus.i.PENDING_RP_CANCEL, VocGisStatus.i.CANCELLED, VocGisStatus.i.FAILED_CANCEL),
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
                case PENDING_RQ_CANCEL:    return CANCEL;
                case PENDING_RP_CANCEL:    return CANCEL;
                default: return null;
            }            
        }

    };        

}