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
        Map<String, Object> r = getData ("86d881af-b360-4370-b9b8-308dd8133484");
        dump (r);
        validate (MeteringDeviceLog.toImportMeteringDeviceData (r));
    }

    private Map<String, Object> getData (String uuid) throws SQLException {
        try (DB db = model.getDb ()) {
            return MeteringDeviceLog.getForExport (db, uuid);
        }
    }

}