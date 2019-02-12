package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import org.junit.Test;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.ws.base.AbstactServiceAsync;
import ru.gosuslugi.dom.schema.integration.house_management.ImportAccountIndividualServicesRequest;

public class AccountIndividualServiceLogTest extends BaseTest {
    
    AccountIndividualService table;
    AccountIndividualServiceLog logTable;    
    
    public AccountIndividualServiceLogTest () throws Exception {
        
        super ();
        jc = JAXBContext.newInstance (ImportAccountIndividualServicesRequest.class);
        schema = AbstactServiceAsync.loadSchema ("house-management/hcs-house-management-types.xsd");
        
        table = (AccountIndividualService) model.get (AccountIndividualService.class);
        logTable = (AccountIndividualServiceLog) model.get (AccountIndividualServiceLog.class);
        
    }

    @Test (expected = Test.None.class)
    public void testInsert () throws SQLException {
        
        Map<String, Object> r = getData ();
        
        r.put ("accountindividualserviceguid", null);        
        dump (r);        
        validate (AccountIndividualServiceLog.toImportAccountIndividualServicesRequest (r));
        
    }
    
    @Test (expected = Test.None.class)
    public void testUpdate () throws SQLException {
        
        Map<String, Object> r = getData ();
                
        r.put ("accountindividualserviceguid", r.get ("uuid"));
        dump (r);        
        validate (AccountIndividualServiceLog.toImportAccountIndividualServicesRequest (r));

    }
    
    @Test (expected = Test.None.class)
    public void testDelete () throws SQLException {
        
        Map<String, Object> r = getData ();
                
        r.put ("accountindividualserviceguid", r.get ("uuid"));
        dump (r);        
        validate (AccountIndividualServiceLog.toDeleteAccountIndividualServicesRequest (r));

    }

    private Map<String, Object> getData () throws SQLException {

        try (DB db = model.getDb ()) {            
            final Map<String, Object> r = db.getMap (logTable.getForExport ("c3d0e3e3-e41b-4cbd-87f7-ced6c4262734"));
            r.put ("attachmentguid", r.get ("uuid"));
            r.put ("attachmenthash", "0000000000000000");
            return r;
        }

    }

}