package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.db.sql.build.QP;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.db.model.voc.VocAction;
import ru.eludia.products.mosgis.ws.base.AbstactServiceAsync;
import ru.gosuslugi.dom.schema.integration.services.ImportWorkingListRequest;

public class WorkingListLogTest extends BaseTest {
    
    private static final UUID uuid = UUID.fromString ("00000000-0000-0000-0000-000000000000");
    
    private WorkingList table;
    private WorkingListLog logTable;
    
    private final Map<String, Object> commonPart;
        
    public WorkingListLogTest () throws Exception {
        
        super ();        

        jc       = JAXBContext.newInstance (ImportWorkingListRequest.class);
        schema   = AbstactServiceAsync.loadSchema ("services/hcs-services-types.xsd");
        
        table    = (WorkingList) model.get (WorkingList.class);
        logTable = (WorkingListLog) model.get (WorkingListLog.class);        
        
        this.commonPart = HASH (
            EnTable.c.UUID, uuid,
            EnTable.c.IS_DELETED, 0,
            WorkingList.c.ID_CTR_STATUS, 10,
            WorkingList.c.ID_CTR_STATUS_GIS, 10,
            WorkingList.c.UUID_CONTRACT_OBJECT, getSomeUuid (ContractObject.class),
            WorkingList.c.UUID_CHARTER_OBJECT, null,
            WorkingList.c.ID_LOG, null
        );        
        
    }
    
    @Before
//    @After
    public void clean () throws SQLException {

        try (DB db = model.getDb ()) {            

            db.d0 (new QP ("UPDATE tb_work_lists SET id_log = NULL WHERE uuid = '00000000000000000000000000000000'"));
            db.d0 (new QP ("DELETE FROM tb_work_lists__log WHERE uuid_object = '00000000000000000000000000000000'"));
            db.d0 (new QP ("DELETE FROM tb_work_lists WHERE uuid = '00000000000000000000000000000000'"));

        }

    }
    
    private String createData (final DB db, Map<String, Object> sample) throws SQLException {
        
        db.insert (table, sample);        
        
        String id = model.createIdLog (db, table, null, uuid, VocAction.i.APPROVE);        
        
        db.update (table, HASH (
            EnTable.c.UUID, uuid,
            WorkingList.c.ID_LOG, id
        ));
        
        return id;
        
    }
    
    private void checkSample (Map<String, Object> rr) throws SQLException {
        
        Map<String, Object> sample = table.new Sampler (commonPart, rr).nextHASH ();
        sample.remove (AgreementPayment.c.IS_ANNULED.lc ());

        try (DB db = model.getDb ()) {            
            String idLog = createData (db, sample);           
            Map<String, Object> r = db.getMap (logTable.getForExport (idLog));

            checkImport (r);

        }
        
    }    
    
    private void checkImport (final Map<String, Object> r) throws IllegalStateException {
        dump (r);
        validate (WorkingListLog.toImportWorkingListRequest (r));
    }
    
    @Test
    public void testInsert () throws SQLException {

        checkSample (HASH (
        ));        

    }

}