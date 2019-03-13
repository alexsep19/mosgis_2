package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import org.junit.Test;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.ws.soap.impl.base.SOAPTools;
import ru.gosuslugi.dom.schema.integration.device_metering.ImportMeteringDeviceValuesRequest;

public class MeteringDeviceValueLogTest extends BaseTest {
    
    public MeteringDeviceValueLogTest () throws Exception {
        super ();
        jc = JAXBContext.newInstance (ImportMeteringDeviceValuesRequest.class);
        schema = SOAPTools.loadSchema ("device-metering/hcs-device-metering-types.xsd");
    }

    @Test (expected = Test.None.class)
    public void test () throws Exception {
        Map<String, Object> r = getData ("370B62AA53BE49C88F0E7C4C66FF9298");
        dump (r);
        validate (MeteringDeviceValueLog.toImportMeteringDeviceValuesRequest (r));
    }

    private Map<String, Object> getData (String uuid) throws SQLException {
        try (DB db = model.getDb ()) {
            return MeteringDeviceValueLog.getForExport (db, uuid);
        }
    }
    
}