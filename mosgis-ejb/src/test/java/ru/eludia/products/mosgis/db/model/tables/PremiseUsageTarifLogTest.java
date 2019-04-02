package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import org.junit.Test;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.ws.soap.tools.SOAPTools;
import ru.gosuslugi.dom.schema.integration.tariff.ImportResidentialPremisesUsageRequest;

public class PremiseUsageTarifLogTest extends BaseTest {
    
    public PremiseUsageTarifLogTest () throws Exception {
        super ();
        jc = JAXBContext.newInstance (ImportResidentialPremisesUsageRequest.class);
        schema = SOAPTools.loadSchema ("tariff/hcs-tariff-types.xsd");
    }

    @Test (expected = Test.None.class)
    public void test () throws SQLException {
        Map<String, Object> r = getData ("8573f8145721336ce0530d0b000afded");
        dump (r);
        validate (PremiseUsageTarifLog.toImportResidentialPremisesUsageRequest (r));
    }
    
    private Map<String, Object> getData (String uuid) throws SQLException {
        try (DB db = model.getDb ()) {
            return PremiseUsageTarifLog.getForExport (db, uuid);
        }
    }
    
}
