package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.gosuslugi.dom.schema.integration.services.ImportWorkingListRequest;

public class WorkingListItem extends EnTable {

    public enum c implements EnColEnum {

        UUID_WORKING_LIST    (WorkingList.class,      "Ссылка на перечень"),
        UUID_ORG_WORK        (OrganizationWork.class, "Ссылка на работу/услугу организации"),
        
        ID_LOG               (WorkingListItemLog.class,  "Последнее событие редактирования"),
        INDEX_               (Type.NUMERIC,  4, 0, null,   "Номер строки в перечне работ и услуг"),
        
        PRICE                (Type.NUMERIC, 14, 4, null,   "Цена"),
        AMOUNT               (Type.NUMERIC, 14, 3, null,   "Объём"),
        COUNT                (Type.NUMERIC,  4, 0, null,   "Количество"),
        TOTALCOST            (Type.NUMERIC, 22, 2, null,   "Общая стоимость"),
        WORKLISTITEMGUID     (Type.UUID,           null,   "Идентификатор работы/услуги перечня")
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
                case UUID_WORKING_LIST:
                    return false;
                default: 
                    return true;
            }
        }

    }

    public WorkingListItem () {

        super ("tb_work_list_items", "Перечни работ и услуг на период");

        cols   (c.class);

        key    ("ndx", c.UUID_WORKING_LIST, c.INDEX_);
        
        trigger ("BEFORE INSERT OR UPDATE", ""                
            + "BEGIN "
            + " IF :NEW.price IS NOT NULL THEN "
            + "  :NEW.totalcost := :NEW.price * NVL (:NEW.amount, 1) * NVL (:NEW.count, 1); "
            + " END IF ; "
            + "END;"
        );
        
        trigger ("AFTER INSERT OR UPDATE", ""
            + "DECLARE" 
            + " PRAGMA AUTONOMOUS_TRANSACTION; "
            + "BEGIN "
            + " UPDATE tb_work_lists SET totalcost = (SELECT SUM(totalcost) FROM tb_work_list_items WHERE uuid_working_list=:NEW.uuid_working_list AND is_deleted=0) WHERE uuid = :NEW.uuid_working_list; "
            + " UPDATE tb_work_lists SET count     = (SELECT SUM(count)     FROM tb_work_list_items WHERE uuid_working_list=:NEW.uuid_working_list AND is_deleted=0) WHERE uuid = :NEW.uuid_working_list; "                                
            + " COMMIT;"
            + "END;"
        );
        
    }
    
    public static void addTo (DB db, Map<String, Object> r) throws SQLException {
        
        NsiTable nsi56 = NsiTable.getNsiTable (56);
        
        r.put ("items", db.getList (db.getModel ()
            .select (WorkingListItem.class, "*")
            .toOne (OrganizationWork.class, "AS w").on ()
            .toOne (nsi56, "AS vc_nsi_56", "code", "guid").on ("vc_nsi_56.code=w.code_vc_nsi_56 AND vc_nsi_56.isactual=1")
            .where (WorkingListItem.c.UUID_WORKING_LIST.lc (), r.get ("uuid_object"))
        ));
        
    }
    
    public static ImportWorkingListRequest.ApprovedWorkingListData.WorkListItem toDom (Map<String, Object> r) {
        r.put ("index", r.get ("index_"));
        final ImportWorkingListRequest.ApprovedWorkingListData.WorkListItem result = DB.to.javaBean (ImportWorkingListRequest.ApprovedWorkingListData.WorkListItem.class, r);
        result.setTotalCost (null);
        result.setTransportGUID (UUID.randomUUID ().toString ());
        result.setWorkItemNSI (NsiTable.toDom (r, "vc_nsi_56"));        
        return result;
    }

}