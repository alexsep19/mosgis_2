package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import org.junit.Test;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.ws.soap.tools.SOAPTools;
import ru.gosuslugi.dom.schema.integration.nsi.ImportGeneralNeedsMunicipalResourceRequest;

public class GeneralNeedsMunicipalResourceLogTest extends BaseTest {
    
    public GeneralNeedsMunicipalResourceLogTest () throws Exception {
        super ();
        jc = JAXBContext.newInstance (ImportGeneralNeedsMunicipalResourceRequest.class);
        schema = SOAPTools.loadSchema ("nsi/hcs-nsi-types.xsd");
    }

    @Test (expected = Test.None.class)
    public void test () throws SQLException {
        Map<String, Object> r = getData ("391B92DDE1A040839CA84DA37CB8B959");
        dump (r);
        validate (GeneralNeedsMunicipalResourceLog.toImportGeneralNeedsMunicipalResourceRequest (r));
    }
    
    private Map<String, Object> getData (String uuid) throws SQLException {
        try (DB db = model.getDb ()) {
            return GeneralNeedsMunicipalResourceLog.getForExport (db, uuid);
        }
    }
    
}
