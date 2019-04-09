package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;

public class OverhaulShortProgramDocument extends EnTable {
    
    public enum c implements EnColEnum {
        
        PROGRAM_UUID        (OverhaulRegionalProgram.class, "Краткосрочная программа"),
        
        CODE_NSI_79         (Type.STRING, 20, "Вид документа"),
        
        NUMBER_             (Type.STRING, 512, "Номер документа"),
        FULLNAME            (Type.STRING, 1000, "Полное имя документа"),
        FULLNAME_UC         (Type.STRING, new Virt ("UPPER(\"FULLNAME\")"), "ПОЛНОЕ ИМЯ ДОКУМЕНТА В ВЕРХНЕМ РЕГИСТРЕ"),
        DATE_               (Type.DATE, "Дата документа"),
        LEGISLATURE         (Type.STRING, 512, "Орган власти, принявший документ"),
        
        ID_LOG              (OverhaulShortProgramDocumentLog.class, "Последнее событие редактирования")
        
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
    
    public OverhaulShortProgramDocument () {
        
        super ("tb_oh_shrt_pr_docs", "Документы краткосрочной программы капитального ремонта");
        
        cols  (c.class);
        
    }
    
}
