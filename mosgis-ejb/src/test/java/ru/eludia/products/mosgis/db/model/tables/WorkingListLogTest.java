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
import ru.eludia.products.mosgis.ws.soap.tools.SOAPTools;
import ru.gosuslugi.dom.schema.integration.services.ImportWorkingListRequest;

public class WorkingListLogTest extends BaseTest {
    
    private static final UUID uuid = UUID.fromString ("00000000-0000-0000-0000-000000000000");
    
    private WorkingList table;
    private WorkingListLog logTable;
    private WorkingListItem itemTable;
    
    private final Map<String, Object> commonPart;
        
    public WorkingListLogTest () throws Exception {
        
        super ();        

        jc       = JAXBContext.newInstance (ImportWorkingListRequest.class);
        schema   = SOAPTools.loadSchema ("services/hcs-services-types.xsd");
        
        table     = (WorkingList) model.get (WorkingList.class);
        logTable  = (WorkingListLog) model.get (WorkingListLog.class);        
        itemTable = (WorkingListItem) model.get (WorkingListItem.class);        
        
        this.commonPart = HASH (
            EnTable.c.UUID, uuid,
            EnTable.c.IS_DELETED, 0,
            WorkingList.c.ID_CTR_STATUS, 10,
            WorkingList.c.ID_CTR_STATUS_GIS, 10,
            WorkingList.c.UUID_CONTRACT_OBJECT, getSomeUuid (ContractObject.class),
            WorkingList.c.UUID_CHARTER_OBJECT, null,
            WorkingList.c.WORKLISTGUID, null,
            WorkingList.c.ID_LOG, null
        );        
        
    }
    
    @Before
    @After
    public void clean () throws SQLException {

        try (DB db = model.getDb ()) {            
            
            db.d0 (new QP ("DELETE FROM tb_work_list_items WHERE uuid_working_list = '00000000000000000000000000000000'"));

            db.d0 (new QP ("UPDATE tb_work_lists SET id_log = NULL WHERE uuid = '00000000000000000000000000000000'"));
            db.d0 (new QP ("DELETE FROM tb_work_lists__log WHERE uuid_object = '00000000000000000000000000000000'"));

            db.d0 (new QP ("DELETE FROM tb_work_plans WHERE uuid_working_list = '00000000000000000000000000000000'"));

            db.d0 (new QP ("DELETE FROM tb_work_lists WHERE uuid = '00000000000000000000000000000000'"));

        }

    }

    private String createData (final DB db, Map<String, Object> sample) throws SQLException {
        
        db.insert (table, sample);        
        createItem (db);
        
        String id = model.createIdLog (db, table, null, uuid, VocAction.i.APPROVE);        
        
        db.update (table, HASH (
            EnTable.c.UUID, uuid,
            WorkingList.c.ID_LOG, id
        ));
                
        return id;
        
    }
    
    private void createItem (final DB db) throws SQLException {
        
        List<Map<String, Object>> works = db.getList (db.getModel ()
            .select (OrganizationWork.class, "uuid")
            .where ("elementguid IS NOT NULL")
            .where ("uniquenumber IS NOT NULL")
            .limit (0, 2)
        );

        db.insert (itemTable, HASH (
            EnTable.c.IS_DELETED, 0,
            "id_log", null,
            WorkingListItem.c.INDEX_, 1,
            WorkingListItem.c.AMOUNT, 2,
            WorkingListItem.c.COUNT, 1,
            WorkingListItem.c.PRICE, 1,
            WorkingListItem.c.UUID_ORG_WORK, works.get (0).get ("uuid"),
            WorkingListItem.c.UUID_WORKING_LIST, uuid
        ));

        db.insert (itemTable, HASH (
            EnTable.c.IS_DELETED, 1,
            "id_log", null,
            WorkingListItem.c.INDEX_, 2,
            WorkingListItem.c.AMOUNT, 3,
            WorkingListItem.c.COUNT, 100,
            WorkingListItem.c.PRICE, 20,
            WorkingListItem.c.UUID_ORG_WORK, works.get (1).get ("uuid"),
            WorkingListItem.c.UUID_WORKING_LIST, uuid
        ));

    }
    
    
    private void checkSample (Map<String, Object> rr) throws SQLException {
        
        Map<String, Object> sample = table.new Sampler (commonPart, rr).nextHASH ();
        sample.remove (AgreementPayment.c.IS_ANNULED.lc ());

        try (DB db = model.getDb ()) {            
            
            String idLog = createData (db, sample);           
            
            Map<String, Object> r = db.getMap (logTable.getForExport (idLog));
            WorkingListItem.addTo (db, r);
            
            if (DB.ok (r.get (WorkingList.c.WORKLISTGUID.lc ()))) {
                checkCancel (r);
            }
            else {
                checkImport (r);
            }            

        }
        
    }    
    
    private void checkImport (final Map<String, Object> r) throws IllegalStateException {
        dump (r);
        validate (WorkingListLog.toImportWorkingListRequest (r));
    }
    
    private void checkCancel (final Map<String, Object> r) throws IllegalStateException {
        dump (r);
        validate (WorkingListLog.toCancelImportWorkingListRequest (r));
    }
    
//    @Ignore    
    @Test
    public void testInsert () throws SQLException {

        checkSample (HASH (
        ));        

    }
    
    @Ignore    
    @Test
    public void testCancel () throws SQLException {

        checkSample (HASH (
            WorkingList.c.WORKLISTGUID, UUID.randomUUID ()
        ));        

    }

}