package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import org.junit.Test;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.ws.soap.tools.SOAPTools;
import ru.gosuslugi.dom.schema.integration.msp.ImportCitizenCompensationRequest;

public class CitizenCompensationLogTest extends BaseTest {
    
    CitizenCompensation table;
    CitizenCompensationLog logTable;
    
    public CitizenCompensationLogTest () throws Exception {
        
        super ();
        jc = JAXBContext.newInstance (ImportCitizenCompensationRequest.class);
        schema = SOAPTools.loadSchema ("msp/hcs-msp-types.xsd");
        
        table = (CitizenCompensation) model.get (CitizenCompensation.class);
        logTable = (CitizenCompensationLog) model.get (CitizenCompensationLog.class);
        
    }

    @Test (expected = Test.None.class)
    public void test () throws Exception {
        
        Map<String, Object> r = getData ();
        
        r.put ("citizencompensationguid", null);

        dump (r);

        validate (CitizenCompensationLog.toImportCitizenCompensationRequest (r));
        
        r.put ("citizencompensationguid", UUID.randomUUID ());

        dump (r);

        validate (CitizenCompensationLog.toImportCitizenCompensationRequest (r));
        
    }

    private Map<String, Object> getData () throws SQLException {
        
        try (DB db = model.getDb ()) {
            
            final Map<String, Object> r = logTable.getForExport (db, "871c7d27-73b9-5e82-e053-0d0b000ae833");
            
            return r;

        }
        
    }
    
}
