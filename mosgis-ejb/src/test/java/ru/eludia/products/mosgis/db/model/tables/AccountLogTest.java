package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import org.junit.Test;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.ws.base.AbstactServiceAsync;
import ru.gosuslugi.dom.schema.integration.house_management.ImportAccountRequest;

public class AccountLogTest extends BaseTest {
    
    Account table;
    AccountLog logTable;
    
    public AccountLogTest () throws Exception {
        
        super ();
        jc = JAXBContext.newInstance (ImportAccountRequest.class);
        schema = AbstactServiceAsync.loadSchema ("house-management/hcs-house-management-types.xsd");
        
        table = (Account) model.get (Account.class);
        logTable = (AccountLog) model.get (AccountLog.class);
        
    }

    @Test (expected = Test.None.class)
    public void test () throws Exception {
        
        Map<String, Object> r = getData ();

        dump (r);

        validate (AccountLog.toImportAccountRequest (r));
        
    }

    private Map<String, Object> getData () throws SQLException {
        
        try (DB db = model.getDb ()) {
            
            final Map<String, Object> r = db.getMap (logTable.getForExport ("9b582a88-e060-405a-8a1d-56baef2cb6f5"));
            
            AccountLog.addPlannedWorksForExport (db, r);
            
            return r;

        }
        
    }
    
}
