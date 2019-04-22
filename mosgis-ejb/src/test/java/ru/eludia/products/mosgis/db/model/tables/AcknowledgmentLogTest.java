package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import org.junit.Test;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.ws.soap.tools.SOAPTools;
import ru.gosuslugi.dom.schema.integration.bills.ImportAcknowledgmentRequest;

public class AcknowledgmentLogTest extends BaseTest {
    
    public AcknowledgmentLogTest () throws Exception {
        super ();
        jc = JAXBContext.newInstance (ImportAcknowledgmentRequest.class);
        schema = SOAPTools.loadSchema ("bills/hcs-bills-types.xsd");
    }

    @Test (expected = Test.None.class)
    public void test () throws Exception {
        Map<String, Object> r = getData ("d24de931-4b09-439c-8b0e-e52e53db51c1");
        dump (r);
        validate (AcknowledgmentLog.toImportAcknowledgment (r));
    }
    
    private Map<String, Object> getData (String uuid) throws SQLException {
        try (DB db = model.getDb ()) {
            return AcknowledgmentLog.getForExport (db, uuid);
        }
    }

}