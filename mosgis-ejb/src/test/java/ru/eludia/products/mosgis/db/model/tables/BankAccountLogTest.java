package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import org.junit.Test;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.ws.soap.tools.SOAPTools;
import ru.gosuslugi.dom.schema.integration.capital_repair.ImportAccountRegionalOperatorRequest;

public class BankAccountLogTest extends BaseTest {
    
    BankAccount table;
    BankAccountLog logTable;
    
    public BankAccountLogTest () throws Exception {
        
        super ();
        jc = JAXBContext.newInstance (ImportAccountRegionalOperatorRequest.class);
        schema = SOAPTools.loadSchema ("capital-repair/hcs-capital-repair-types.xsd");
        
        table = (BankAccount) model.get (BankAccount.class);
        logTable = (BankAccountLog) model.get (BankAccountLog.class);
        
    }

    @Test (expected = Test.None.class)
    public void test () throws Exception {
        
        Map<String, Object> r = getData ();
        
        r.put ("accountregoperatorguid", null);

        dump (r);

        validate (BankAccountLog.toImportAccountRegionalOperatorRequest(r));
        
        r.put ("accountregoperatorguid", UUID.randomUUID ());

        dump (r);

        validate (BankAccountLog.toImportAccountRegionalOperatorRequest (r));
        
    }

    private Map<String, Object> getData () throws SQLException {
        
        try (DB db = model.getDb ()) {
            
            final Map<String, Object> r = db.getMap(logTable.getForExport ("8730c6ab-75e4-2788-e053-0d0b000a09a9"));

	    return r;

        }
        
    }
    
}
