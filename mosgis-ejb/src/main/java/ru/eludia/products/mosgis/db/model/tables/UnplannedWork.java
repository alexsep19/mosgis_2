package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.gosuslugi.dom.schema.integration.services.CompletedWorksByPeriodType;
import ru.gosuslugi.dom.schema.integration.services.MonthlyWorkType;

public class UnplannedWork extends EnTable {
 
    public enum c implements EnColEnum {

        UUID_REPORTING_PERIOD  (ReportingPeriod.class,       "Ссылка период отчётности"),
//        DAYS_BITMASK           (Type.BINARY,  5,       null, "Битовая маска чисел месяца (например, 0x4001 — 31-е и 1-е числа)"),        

        PRICE                  (Type.NUMERIC, 14, 4, null,   "Фактическая цена"),
        AMOUNT                 (Type.NUMERIC, 14, 3, null,   "Фактический объём"),
        COUNT                  (Type.NUMERIC,  4, 0, null,   "Количество работ по плану"),        
        TOTALCOST              (Type.NUMERIC, 22, 2, null,   "Фактическая стоимость выполненных работ"),
        COMMENT_               (Type.TEXT,           null,   "Комментарий"),
        UUID_ORG_WORK          (OrganizationWork.class,      "Ссылка на работу/услугу организации"),

        CODE_VC_NSI_3          (Type.STRING,  20,    null,   "Вид КУ (НСИ 3)"),
        
        ORGANIZATIONGUID       (VocOrganization.class,       "Поставщик коммунального ресурса"),

        ACCIDENTREASON         (Type.TEXT,           null,   "Комментарий"),
        CODE_VC_NSI_57         (Type.STRING,  20,    null,   "Ссылка на объект аварии (НСИ №57)"),

        ;

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

        @Override
        public boolean isLoggable () {
            switch (this) {
//                case ID_LOG:
//                    return false;
                default: 
                    return true;
            }
        }

    }

    public UnplannedWork () {
        
        super  ("tb_unplanned_works", "Выполненная внеплановая работа/услуга");        
        cols   (c.class);
        
        trigger ("BEFORE UPDATE", ""
                
            + "BEGIN "
                + ":NEW.totalcost := :NEW.price * NVL (:NEW.amount, 1) * NVL (:NEW.count, 1); "
            + "END;"                
                
        );
        
    }
    
    static CompletedWorksByPeriodType.UnplannedWork toUnplannedWork (Map<String, Object> r) {
        r.put ("comment", r.get ("comment_"));
        final CompletedWorksByPeriodType.UnplannedWork result = DB.to.javaBean (CompletedWorksByPeriodType.UnplannedWork.class, r);
        result.setMonthlyWork (toMonthlyWork (r));
        result.setWork (NsiTable.toDom (r.get ("ow.uniquenumber").toString (), (UUID) r.get ("ow.elementguid")));
        result.setWorkType (NsiTable.toDom (r, "vc_nsi_56"));
        return result;            
    }
    
    private static MonthlyWorkType toMonthlyWork (Map<String, Object> r) {
        final MonthlyWorkType result = DB.to.javaBean (MonthlyWorkType.class, r);
        return result;
    }
    
}