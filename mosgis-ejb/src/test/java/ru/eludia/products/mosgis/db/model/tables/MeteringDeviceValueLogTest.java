package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import org.junit.Test;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.ws.base.AbstactServiceAsync;
import ru.gosuslugi.dom.schema.integration.device_metering.ImportMeteringDeviceValuesRequest;

public class MeteringDeviceValueLogTest extends BaseTest {
    
    public MeteringDeviceValueLogTest () throws Exception {
        super ();
        jc = JAXBContext.newInstance (ImportMeteringDeviceValuesRequest.class);
        schema = AbstactServiceAsync.loadSchema ("device-metering/hcs-device-metering-types.xsd");
    }

    @Test (expected = Test.None.class)
    public void test () throws Exception {
        Map<String, Object> r = getData ("eb6167af-8cd0-4ffa-91d6-d24fca15decd");
        dump (r);
        validate (MeteringDeviceValueLog.toImportMeteringDeviceValuesRequest (r));
    }

    private Map<String, Object> getData (String uuid) throws SQLException {
        try (DB db = model.getDb ()) {
            return MeteringDeviceValueLog.getForExport (db, uuid);
        }
    }
    
}