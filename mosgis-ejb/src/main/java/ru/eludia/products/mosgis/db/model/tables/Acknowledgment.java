package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;

public class Acknowledgment extends EnTable {
    
    public static final String TABLE_NAME = "tb_pay_ack";
    
    public enum c implements EnColEnum {
        
        UUID_PAY_DOC          (PaymentDocument.class,       "Платёжный документ"),
        UUID_PAY              (Payment.class,               "Платёж"),
        AMOUNT                (Type.NUMERIC, 13, 2, null,   "Размер превышения платы, рассчитанной с применением повышающего коэффициента над размером платы, рассчитанной без учета повышающего коэффициента, руб."),
        
	ID_CTR_STATUS         (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения mosgis"),
	ID_CTR_STATUS_GIS     (VocGisStatus.class,    VocGisStatus.DEFAULT, "Статус с точки зрения ГИС ЖКХ"),

        ID_LOG                (AcknowledgmentLog.class,     "Последнее событие редактирования"),

        ;
        
        @Override
        public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}
        
        @Override
        public boolean isLoggable () {
            switch (this) {
//                case ID_LOG:
                case UUID_PAY:
                case UUID_PAY_DOC:
                    return false;
                default:
                    return true;
            }
        }        
        
    }

    public Acknowledgment () {
        
        super  (TABLE_NAME, "Записи квитирования");

        cols   (c.class);        

        key (c.UUID_PAY);
        key (c.UUID_PAY_DOC);
/*        
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
*/        
    }    

}