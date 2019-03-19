package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class OverhaulRegionalProgram extends EnTable {
    
    public enum c implements EnColEnum {
        
        ID_ORP_STATUS       (VocGisStatus.class, VocGisStatus.i.PROJECT.asDef (), "Статус вида работ капитального ремонта с точки зрения mosgis"),
        ID_ORP_STATUS_GIS   (VocGisStatus.class, VocGisStatus.i.PROJECT.asDef (), "Статус вида работ капитального ремонта с точки зрения ГИС ЖКХ"),
        
        ORG_UUID            (VocOrganization.class, null, "Поставщик информации"),
        
        PROGRAMNAME         (Type.STRING, 1000, "Наименование программы"),
        STARTYEAR           (Type.NUMERIC, 4, "Год начала периода реализации"),
        ENDYEAR             (Type.NUMERIC, 4, "Год окончания периода реализации"),
        
        ID_LOG              (OverhaulRegionalProgramLog.class, "Последнее событие редактирования")
        
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
    
    public OverhaulRegionalProgram () {
        
        super ("tb_oh_reg_programs", "Региональные программы капитального ремонта");
        
        cols  (c.class);
        
    }
    
}