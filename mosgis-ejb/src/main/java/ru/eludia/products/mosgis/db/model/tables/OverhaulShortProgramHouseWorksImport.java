package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.INTEGER;
import static ru.eludia.base.model.Type.TIMESTAMP;
import static ru.eludia.base.model.Type.UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;

public class OverhaulShortProgramHouseWorksImport extends EnTable {
    
    public enum c implements EnColEnum {
        
        ORGPPAGUID    (UUID,           "Идентификатор зарегистрированной организации"),
        PROGRAM_UUID  (OverhaulShortProgram.class, "Краткосрочная программа"),
        
        COUNT         (INTEGER,  null, "Количество импортируемых в ГИС видов работ"),
        OK_COUNT      (INTEGER,  null, "Количество успешно импортированных в ГИС видов работ"),
        TS            (TIMESTAMP, NOW, "Дата/время начала импорта"),
        
        UUID_OUT_SOAP (OutSoap.class, null, "Запрос на импорт в ГИС ЖКХ"),
        UUID_MESSAGE  (UUID, null, "UUID запроса в ГИС ЖКХ")
        
        ;
        
        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}        
        @Override
        public boolean isLoggable () {
            return false;
        }
        
    }
    
    public OverhaulShortProgramHouseWorksImport () {
        
        super   ("tb_oh_shrt_pr_works_imports", "Записи импорта видов работ краткосрочной программы капитального ремонта");
        
        cols    (c.class);
        
    }
    
}
