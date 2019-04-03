package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import org.junit.Test;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.ws.soap.tools.SOAPTools;
import ru.gosuslugi.dom.schema.integration.nsi.ImportBaseDecisionMSPRequest;

public class BaseDecisionMSPLogTest extends BaseTest {
    
    public BaseDecisionMSPLogTest () throws Exception {
        super ();
        jc = JAXBContext.newInstance (ImportBaseDecisionMSPRequest.class);
        schema = SOAPTools.loadSchema ("nsi/hcs-nsi-types.xsd");
    }

    @Test (expected = Test.None.class)
    public void test () throws SQLException {
        Map<String, Object> r = getData ("859FC19226A66348E0530D0B000AA58C");
        dump (r);
        validate (BaseDecisionMSPLog.toImportBaseDecisionMSPRequest (r));
    }
    
    private Map<String, Object> getData (String uuid) throws SQLException {
        try (DB db = model.getDb ()) {
            return BaseDecisionMSPLog.getForExport (db, uuid);
        }
    }
    
}
