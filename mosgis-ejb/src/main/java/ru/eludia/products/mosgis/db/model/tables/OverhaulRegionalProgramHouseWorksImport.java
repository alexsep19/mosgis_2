package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.DATE;
import static ru.eludia.base.model.Type.INTEGER;
import static ru.eludia.base.model.Type.NUMERIC;
import static ru.eludia.base.model.Type.STRING;
import static ru.eludia.base.model.Type.TIMESTAMP;
import static ru.eludia.base.model.Type.UUID;
import static ru.eludia.base.model.def.Def.NOW;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;

public class OverhaulRegionalProgramHouseWorksImport extends EnTable {
    
    public enum c implements EnColEnum {
        
        COUNT                 (INTEGER, "Количество импортируемых в ГИС видов работ"),
        OK_COUNT              (INTEGER, null, "Количество успешно импортированных в ГИС видов работ"),
        TS                    (TIMESTAMP, NOW, "Дата/время начала импорта")
        
        ;
        
        @Override public Col getCol () {return col;} private Col col; private c (Type type, Object... p) {col = new Col (this, type, p);} private c (Class c,   Object... p) {col = new Ref (this, c, p);}        
        @Override
        public boolean isLoggable () {
            return false;
        }
        
    }
    
    public OverhaulRegionalProgramHouseWorksImport () {
        
        super ("tb_oh_reg_pr_works_imports", "Записи импорта видов работ региональной программы капитального ремонта");
        
        cols  (c.class);
        
    }
    
}
