package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.DATE;
import ru.eludia.base.model.def.Num;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;

public class WorkingList extends EnTable {

    public enum c implements EnColEnum {

        UUID_CONTRACT_OBJECT (ContractObject.class,  "Ссылка на объект договора"),
        UUID_CHARTER_OBJECT  (CharterObject.class,   "Ссылка на объект устава"),
        FIASHOUSEGUID        (VocBuilding.class,     "Дом"),
        ID_CTR_STATUS        (VocGisStatus.class,    new Num (VocGisStatus.i.PROJECT.getId ()), "Статус объекта договора с точки зрения mosgis"),
        ID_CTR_STATUS_GIS    (VocGisStatus.class,    new Num (VocGisStatus.i.PROJECT.getId ()), "Статус объекта договора с точки зрения ГИС ЖКХ"),
        ID_LOG               (WorkingListLog.class,  "Последнее событие редактирования"),
        DT_FROM              (DATE,                  "Период \"с\"  (первое число первого месяца периода)"),
        DT_TO                (DATE,                  "Период \"по\" (последнее число последнего месяца периода)")
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
                case UUID_CONTRACT_OBJECT:
                case UUID_CHARTER_OBJECT:
                case FIASHOUSEGUID:
                    return false;
                default: 
                    return true;
            }
        }

    }

    public WorkingList () {

        super ("tb_work_lists", "Перечни работ и услуг на период");

        cols   (c.class);

        key    ("fiashouseguid", c.FIASHOUSEGUID);                
        key    ("uuid_contract_object", c.UUID_CONTRACT_OBJECT);                
        key    ("uuid_charter_object", c.UUID_CHARTER_OBJECT);                
        
        trigger ("BEFORE INSERT", 
                
              "BEGIN"

            + " IF :NEW.uuid_contract_object IS NOT NULL THEN "
                + "SELECT fiashouseguid INTO :NEW.fiashouseguid FROM tb_contract_objects WHERE uuid=:NEW.uuid_contract_object; "
            + " END IF;"
                      
            + " IF :NEW.uuid_charter_object IS NOT NULL THEN "
                + "SELECT fiashouseguid INTO :NEW.fiashouseguid FROM tb_charter_objects WHERE uuid=:NEW.uuid_charter_object; "
            + " END IF;"
                      
            + "END;"
                
        );

    }

}