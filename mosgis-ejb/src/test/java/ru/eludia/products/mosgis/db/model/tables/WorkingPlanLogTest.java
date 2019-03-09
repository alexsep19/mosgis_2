package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.build.QP;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.ws.soap.impl.base.AbstactServiceAsync;
import ru.gosuslugi.dom.schema.integration.services.ImportWorkingPlanRequest;
import ru.gosuslugi.dom.schema.integration.services.ExportWorkingPlanRequest;
import ru.eludia.products.mosgis.db.model.tables.WorkingPlan.c;

public class WorkingPlanLogTest extends BaseTest {
    
    private static final UUID uuid = UUID.fromString ("00000000-0000-0000-0000-000000000000");
    
    private WorkingPlan table;
    private WorkingPlanLog logTable;
    private WorkingPlanItem itemTable;
//    private WorkingList listTable;
//    private WorkingListLog listLogTable;
//    private WorkingListItem listItemTable;
    
    private Map<String, Object> commonPart;    

    public WorkingPlanLogTest () throws Exception {
        
        super ();        

        jc            = JAXBContext.newInstance (ImportWorkingPlanRequest.class, ExportWorkingPlanRequest.class);
        schema        = AbstactServiceAsync.loadSchema ("services/hcs-services-types.xsd");
        
        table         = (WorkingPlan) model.get (WorkingPlan.class);
        logTable      = (WorkingPlanLog) model.get (WorkingPlanLog.class);        
        itemTable     = (WorkingPlanItem) model.get (WorkingPlanItem.class);        
//        listTable     = (WorkingList) model.get (WorkingList.class);
//        listLogTable  = (WorkingListLog) model.get (WorkingListLog.class);        
//        listItemTable = (WorkingListItem) model.get (WorkingListItem.class);        
        
        this.commonPart = HASH (
            EnTable.c.UUID, uuid,
            EnTable.c.IS_DELETED, 0,
            c.ID_CTR_STATUS, 10,
            c.ID_CTR_STATUS_GIS, 10,            
            c.UUID_WORKING_LIST, getSomeUuid (WorkingList.class),
            c.ID_LOG, null,
            c.YEAR, 2019            
        );        
        
    }
    
    @Before
    @After
    public void clean () throws SQLException {

        try (DB db = model.getDb ()) {            
            
            db.d0 (new QP ("DELETE FROM tb_work_plan_items WHERE uuid_working_plan = '00000000000000000000000000000000'"));

            db.d0 (new QP ("UPDATE tb_work_plans SET id_log = NULL WHERE uuid = '00000000000000000000000000000000'"));
            db.d0 (new QP ("DELETE FROM tb_work_plans__log WHERE uuid_object = '00000000000000000000000000000000'"));

            db.d0 (new QP ("DELETE FROM tb_work_plans WHERE uuid = '00000000000000000000000000000000'"));

        }

    }    
    
    private String createData (final DB db, Map<String, Object> sample) throws SQLException {
        
        db.insert (table, sample);        
        createItem (db);
        
        String id = model.createIdLog (db, table, null, uuid, VocAction.i.APPROVE);        
        
        db.update (table, HASH (
            EnTable.c.UUID, uuid,
            c.ID_LOG, id
        ));
                
        return id;
        
    }    
    
    private void createItem (final DB db) throws SQLException {
        
        String workingListItemGuid = getSomeUuid (WorkingListItem.class);

        db.insert (itemTable, HASH (
            EnTable.c.IS_DELETED, 0,
            "id_log", null,
//            WorkingPlanItem.c.DAYS_BITMASK, ((1 << 31) | (1 << 15) | 1),
            WorkingPlanItem.c.DAYS_BITMASK, (1 << 30) | (1 << 14) | 1,
            WorkingPlanItem.c.MONTH, 1,
            WorkingPlanItem.c.UUID_WORKING_LIST_ITEM, workingListItemGuid,
            WorkingPlanItem.c.UUID_WORKING_PLAN, uuid,
            WorkingPlanItem.c.WORKCOUNT, 3
        ));
        
        db.insert (itemTable, HASH (
            EnTable.c.IS_DELETED, 0,
            "id_log", null,
            WorkingPlanItem.c.DAYS_BITMASK, null,
            WorkingPlanItem.c.MONTH, 2,
            WorkingPlanItem.c.UUID_WORKING_LIST_ITEM, workingListItemGuid,
            WorkingPlanItem.c.UUID_WORKING_PLAN, uuid,
            WorkingPlanItem.c.WORKCOUNT, 5
        ));

    }
    
    private void checkSample (Map<String, Object> rr) throws SQLException {
        
        Map<String, Object> sample = table.new Sampler (commonPart, rr).nextHASH ();

        try (DB db = model.getDb ()) {            
            
            String idLog = createData (db, sample);           
            
            Map<String, Object> r = db.getMap (logTable.getForExport (idLog));
            r.put ("worklistguid", UUID.randomUUID ());

            WorkingPlanItem.addTo (db, r);            
            for (Map<String, Object> i: (List<Map<String, Object>>) r.get ("items")) i.put ("worklistitemguid", UUID.randomUUID ());

            checkImport (r);
            checkExport (r);

        }
        
    }   
    
    private void checkImport (final Map<String, Object> r) throws IllegalStateException {
        dump (r);
        validate (WorkingPlanLog.toImportWorkingPlanRequest (r));
    }
    
    private void checkExport (final Map<String, Object> r) throws IllegalStateException {
        dump (r);
        validate (WorkingPlanLog.toExportWorkingPlanRequest (r));
    }
    
    @Ignore
    @Test
    public void testInsert () throws SQLException {

        checkSample (HASH (
        ));        

    }

}