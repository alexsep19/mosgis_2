package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import org.junit.Test;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.ws.soap.impl.base.AbstactServiceAsync;
import ru.gosuslugi.dom.schema.integration.bills.ImportRSOSettlementsRequest;

public class SettlementDocLogTest extends BaseTest {
    
    SettlementDoc table;
    SettlementDocLog logTable;
    
    public SettlementDocLogTest () throws Exception {
        
        super ();
        jc = JAXBContext.newInstance (ImportRSOSettlementsRequest.class);
        schema = AbstactServiceAsync.loadSchema ("bills/hcs-bills-types.xsd");
        
        table = (SettlementDoc) model.get (SettlementDoc.class);
        logTable = (SettlementDocLog) model.get (SettlementDocLog.class);
        
    }

    private Map<String, Object> getData () throws SQLException {
        
        try (DB db = model.getDb ()) {
            
            final Map<String, Object> r = db.getMap (logTable.getForExport ("819a73bc3ab573b6e0530100007fc2cb"));
            
            SettlementDocLog.addItemsForExport (db, r);
            
            return r;

        }
        
    }

    @Test(expected = Test.None.class)
    public void testInsertRSO() throws SQLException {

	Map<String, Object> r = getData();

	r.put("settlementguid", null);
	dump(r);
	validate(SettlementDocLog.toImportRSOSettlementsRequest(r));

    }

    @Test(expected = Test.None.class)
    public void testUpdateRSO() throws SQLException {

	Map<String, Object> r = getData();

	r.put("settlementguid", r.get("uuid"));
	dump(r);
	validate(SettlementDocLog.toImportRSOSettlementsRequest(r));

    }

    @Test(expected = Test.None.class)
    public void testAnnulRSO() throws SQLException {

	Map<String, Object> r = getData();

	r.put("settlementguid", r.get("uuid"));
	r.put("reasonofannulment", r.get("uuid"));
	dump(r);
	validate(SettlementDocLog.toAnnulRSOSettlementsRequest(r));
    }
}
