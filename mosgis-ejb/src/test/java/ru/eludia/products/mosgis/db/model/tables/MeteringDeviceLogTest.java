package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import org.junit.Test;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.ws.base.AbstactServiceAsync;
import ru.gosuslugi.dom.schema.integration.house_management.ImportMeteringDeviceDataRequest;

public class MeteringDeviceLogTest extends BaseTest {

    public MeteringDeviceLogTest () throws Exception {
        super ();
        jc = JAXBContext.newInstance (ImportMeteringDeviceDataRequest.class);
        schema = AbstactServiceAsync.loadSchema ("house-management/hcs-house-management-types.xsd");
    }

    @Test (expected = Test.None.class)
    public void test () throws Exception {
//        Map<String, Object> r = getData ("e24236c7-70a0-465c-addc-809c0bb4ba3f");
//        Map<String, Object> r = getData ("3925017a-09a2-4fbf-96a8-54080db0275d");
        Map<String, Object> r = getData ("2857b1e5-f0ab-4285-aabc-ee15deb1f9d3");

//        r.put (MeteringDevice.c., null);        
        dump (r);
        validate (MeteringDeviceLog.toImportMeteringDeviceData (r));
    }

    private Map<String, Object> getData (String uuid) throws SQLException {
        try (DB db = model.getDb ()) {
            return MeteringDeviceLog.getForExport (db, uuid);
        }
    }

}