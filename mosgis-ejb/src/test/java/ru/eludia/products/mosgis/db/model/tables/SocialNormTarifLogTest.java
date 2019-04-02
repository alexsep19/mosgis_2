package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import org.junit.Test;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.ws.soap.tools.SOAPTools;
import ru.gosuslugi.dom.schema.integration.tariff.ImportSocialNormsRequest;

public class SocialNormTarifLogTest extends BaseTest {
    
    public SocialNormTarifLogTest () throws Exception {
        super ();
        jc = JAXBContext.newInstance (ImportSocialNormsRequest.class);
        schema = SOAPTools.loadSchema ("tariff/hcs-tariff-types.xsd");
    }

    @Test (expected = Test.None.class)
    public void test () throws SQLException {
        Map<String, Object> r = getData ("85879B78AA5F7356E0530D0B000A8647");
        dump (r);
        validate (SocialNormTarifLog.toImportSocialNormsRequest(r));
    }
    
    private Map<String, Object> getData (String uuid) throws SQLException {
        try (DB db = model.getDb ()) {
            return SocialNormTarifLog.getForExport (db, uuid);
        }
    }
    
}
