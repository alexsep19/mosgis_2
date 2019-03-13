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
import ru.eludia.products.mosgis.ws.soap.impl.base.SOAPTools;
import ru.gosuslugi.dom.schema.integration.services.ImportCompletedWorksRequest;

public class ReportingPeriodLogTest extends BaseTest {
    
    private static final UUID uuid = UUID.fromString ("7e85cdc4-456d-4770-95ce-ca822866c43a");

    private ReportingPeriod table;
    private ReportingPeriodLog logTable;    
    
    public ReportingPeriodLogTest () throws Exception {
        
        super ();        
        
        jc            = JAXBContext.newInstance (ImportCompletedWorksRequest.class);
        schema        = SOAPTools.loadSchema ("services/hcs-services-types.xsd");
        
        table         = (ReportingPeriod) model.get (ReportingPeriod.class);
        logTable      = (ReportingPeriodLog) model.get (ReportingPeriodLog.class);        

    }
    
    @Before
    @After
    public void clean () throws SQLException {

        try (DB db = model.getDb ()) {                        
            String u = "'" + uuid.toString ().replaceAll ("-", "").toUpperCase () + "'";
            db.d0 (new QP ("UPDATE tb_reporting_periods SET id_log=NULL WHERE uuid = " + u));
            db.d0 (new QP ("DELETE FROM tb_reporting_periods__log WHERE uuid_object = " + u));
        }

    }    
    
    private String createData (final DB db) throws SQLException {
                
        String id = model.createIdLog (db, table, null, uuid, VocAction.i.APPROVE);        
        
        db.update (table, HASH (
            EnTable.c.UUID, uuid,
            ReportingPeriod.c.ID_LOG, id
        ));
                
        return id;
        
    }    
    
    Map<String, Object> getData () throws SQLException {
        
        try (DB db = model.getDb ()) {            
            
            String idLog = createData (db);
            
            Map<String, Object> r = db.getMap (logTable.getForExport (idLog));
            ReportingPeriodLog.addPlannedWorksForExport (db, r);
            ReportingPeriodLog.addUnplannedWorksForExport (db, r);
            
            return r;
            
        }        
        
    }

    @Test
    public void test () throws SQLException {
        
        Map<String, Object> r = getData ();

        dump (r);

        validate (ReportingPeriodLog.toCompletedWorksRequest (r));
        
    }
    
}
