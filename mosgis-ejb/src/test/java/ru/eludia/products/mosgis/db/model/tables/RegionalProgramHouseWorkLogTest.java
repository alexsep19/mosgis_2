package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import org.junit.Test;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.ws.soap.tools.SOAPTools;
import ru.gosuslugi.dom.schema.integration.capital_repair.ImportRegionalProgramWorkRequest;
import ru.gosuslugi.dom.schema.integration.capital_repair.ExportRegionalProgramWorkRequest;

public class RegionalProgramHouseWorkLogTest extends BaseTest {
    
    private static final String uuid = "5f349c25-cdfb-4249-95a3-3cb52110668f";
    
    private OverhaulRegionalProgramHouseWork table;
    private OverhaulRegionalProgramHouseWorkLog logTable;
    
    public RegionalProgramHouseWorkLogTest() throws Exception {
        
        super ();
        
        jc            = JAXBContext.newInstance (ImportRegionalProgramWorkRequest.class, ExportRegionalProgramWorkRequest.class);
        schema        = SOAPTools.loadSchema ("capital-repair/hcs-capital-repair-types.xsd");
        
        table = (OverhaulRegionalProgramHouseWork) model.get (OverhaulRegionalProgramHouseWork.class);
        logTable = (OverhaulRegionalProgramHouseWorkLog) model.get(OverhaulRegionalProgramHouseWorkLog.class);
        
    }
    
    private void checkSample (Map<String, Object> rr) throws SQLException {
        
        try (DB db = model.getDb ()) {
            
            Map<String, Object> record = OverhaulRegionalProgramHouseWorkLog.getForExport (db, uuid);
            
            System.out.println (record);
            
            checkImport (record);
            
        }
        
    }
    
    private void checkImport (final Map<String, Object> r) throws IllegalStateException {
        dump (r);
        validate (OverhaulRegionalProgramHouseWorkLog.toImportRegionalProgramWorkRequest (r));
    }
    
    //@Ignore
    @Test
    public void testInsert () throws SQLException {

        checkSample (HASH (
        ));        

    }
    
}
