package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import org.junit.Ignore;
import org.junit.Test;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.ws.soap.tools.SOAPTools;
import ru.gosuslugi.dom.schema.integration.payment.ImportSupplierNotificationsOfOrderExecutionRequest;

public class PaymentLogTest extends BaseTest {
    
    Payment table;
    PaymentLog logTable;
    
    public PaymentLogTest () throws Exception {
        
        super ();
        jc = JAXBContext.newInstance (ImportSupplierNotificationsOfOrderExecutionRequest.class);
        schema = SOAPTools.loadSchema ("payment/hcs-payment-types.xsd");
        
        table = (Payment) model.get (Payment.class);
        logTable = (PaymentLog) model.get (PaymentLog.class);
        
    }

    private Map<String, Object> getData() throws SQLException {

	try (DB db = model.getDb()) {
	    final Map<String, Object> r = logTable.getForExport(db, "862af6ca-310a-7b63-e053-0d0b000a79d7");
	    return r;
	}
    }

    @Test (expected = Test.None.class)
    public void test () throws SQLException {
        
        Map<String, Object> r = getData ();
        
        dump (r);        
        validate (PaymentLog.toImportSupplierNotificationsOfOrderExecutionRequest (r));
        
    }
}