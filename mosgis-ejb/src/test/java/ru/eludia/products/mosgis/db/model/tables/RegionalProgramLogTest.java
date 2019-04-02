package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import org.junit.Ignore;
import org.junit.Test;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.ws.soap.tools.SOAPTools;
import ru.gosuslugi.dom.schema.integration.capital_repair.ImportRegionalProgramRequest;
import ru.gosuslugi.dom.schema.integration.capital_repair.ExportRegionalProgramRequest;

public class RegionalProgramLogTest extends BaseTest {
    
    private static final String uuid = "7350e38d-141e-493e-925e-8620a9ed0a45";
    
    private OverhaulRegionalProgram table;
    private OverhaulRegionalProgramLog logTable;
    
    public RegionalProgramLogTest() throws Exception {
        
        super ();
        
        jc            = JAXBContext.newInstance (ImportRegionalProgramRequest.class, ExportRegionalProgramRequest.class);
        schema        = SOAPTools.loadSchema ("capital-repair/hcs-capital-repair-types.xsd");
        
        table = (OverhaulRegionalProgram) model.get (OverhaulRegionalProgram.class);
        logTable = (OverhaulRegionalProgramLog) model.get(OverhaulRegionalProgramLog.class);
        
    }
    
    private void checkSample (Map<String, Object> rr) throws SQLException {
        
        try (DB db = model.getDb ()) {
            
            Map<String, Object> record = OverhaulRegionalProgramLog.getForExport (db, uuid);
            
            System.out.println (record);
            
            checkImport (record);
            
        }
        
    }
    
    private void checkImport (final Map<String, Object> r) throws IllegalStateException {
        dump (r);
        validate (OverhaulRegionalProgramLog.toImportRegionalProgramRequest (r, false));
    }
    
    //@Ignore
    @Test
    public void testInsert () throws SQLException {

        checkSample (HASH (
        ));        

    }
    
}
