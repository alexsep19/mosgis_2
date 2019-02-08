package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.NUMERIC;
import static ru.eludia.base.model.Type.STRING;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocOkei;

public class InfrastructureTransportationResource extends EnTable {
    
    public enum c implements EnColEnum {
        
        UUID_OKI (Infrastructure.class, "Ссылка на объект ОКИ"),
        
        CODE_VC_NSI_2  (STRING, 20, "Ресурс (НСИ 2)"),
        
        TOTALLOAD      (NUMERIC, 9, 3, null, "Присоединенная нагрузка"),
        INDUSTRIALLOAD (NUMERIC, 9, 3, null, "Промышленность"),
        SOCIALLOAD     (NUMERIC, 9, 3, null, "Социальная сфера"),
        POPULATIONLOAD (NUMERIC, 9, 3, null, "Население"),
        
        VOLUMELOSSES   (NUMERIC, 9, 3, "Объем потерь"),
        
        CODE_VC_NSI_41 (STRING, 20, null, "Вид теплоносителя (НСИ 41)"),
        
        OKEI           (VocOkei.class, "Единицы измерения (ОКЕИ)"),
        
        ID_LOG         (InfrastructureTransportationResourceLog.class, "Посленее событие редактирования")
        
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
            switch (this) {
                case UUID_OKI:
                case ID_LOG:
                    return false;
                default:
                    return true;
            }
        }
        
    }
    
    public InfrastructureTransportationResource () {
        
        super ("tb_oki_tr_resources", "Передачи коммунальных ресурсов ОКИ");
        
        cols (c.class);
        
        trigger ("BEFORE INSERT OR UPDATE", ""
                + "DECLARE " 
                    + "PRAGMA AUTONOMOUS_TRANSACTION; "
                + "BEGIN "
                    + "IF :NEW.totalload < NVL(:NEW.industrialload, 0) + NVL(:NEW.socialload, 0) + NVL(:NEW.populationload, 0) THEN "
                        + "raise_application_error (-20000, 'Значение присоединенной нагрузки не должно быть меньше, чем сумма составляющих нагрузок'); "
                    + "END IF; "
                    + "FOR i IN (SELECT res.uuid FROM tb_oki_tr_resources res WHERE res.is_deleted <> 1 AND res.uuid_oki = :NEW.uuid_oki AND res.code_vc_nsi_2 = :NEW.code_vc_nsi_2) LOOP "
                        + "IF i.uuid <> :NEW.uuid THEN "
                            + "raise_application_error (-20000, 'Данный ресурс уже добавлен для передачи коммунальных ресурсов'); "
                        + "END IF; "
                    + "END LOOP; "
                + "END;"
        );
        
    }
    
}
