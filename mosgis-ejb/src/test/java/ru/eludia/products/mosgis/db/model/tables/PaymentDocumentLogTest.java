package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import org.junit.Test;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.ws.soap.tools.SOAPTools;
import ru.gosuslugi.dom.schema.integration.bills.ImportPaymentDocumentRequest;

public class PaymentDocumentLogTest extends BaseTest {
    
    public PaymentDocumentLogTest () throws Exception {
        super ();
        jc = JAXBContext.newInstance (ImportPaymentDocumentRequest.class);
        schema = SOAPTools.loadSchema ("bills/hcs-bills-types.xsd");
    }

    @Test (expected = Test.None.class)
    public void test () throws Exception {
        Map<String, Object> r = getData ("23f23dd9-d633-4556-a6d7-5ff1f2b840d0");
        dump (r);
        validate (PaymentDocumentLog.toImportPaymentDocumentRequest (r));
    }

    private Map<String, Object> getData (String uuid) throws SQLException {
        try (DB db = model.getDb ()) {
            return PaymentDocumentLog.getForExport (db, uuid);
        }
    }
    
}
