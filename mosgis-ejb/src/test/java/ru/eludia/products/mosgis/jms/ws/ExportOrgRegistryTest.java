package ru.eludia.products.mosgis.jms.ws;

import javax.xml.bind.JAXBContext;
import org.junit.Test;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.gosuslugi.dom.schema.integration.base.BaseAsyncResponseType;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common.ExportOrgRegistryRequest;
import ru.gosuslugi.dom.schema.integration.organizations_registry_common.GetStateResult;


public class ExportOrgRegistryTest extends BaseTest {
    
    public ExportOrgRegistryTest () throws Exception {
        jc = JAXBContext.newInstance (GetStateResult.class);
    }

    @Test (expected = Test.None.class)
    public void test () throws Exception {
        
        ExportOrgRegistryRequest exportOrgRegistryRequest = new ExportOrgRegistryRequest ();
        final ExportOrgRegistryRequest.SearchCriteria searchCriteria = new ExportOrgRegistryRequest.SearchCriteria ();
//        searchCriteria.setOGRN ("5147746267939");
        searchCriteria.setOGRNIP ("311500135000061");
        exportOrgRegistryRequest.getSearchCriteria ().add (searchCriteria);
        
        try (DB db = model.getDb ()) {            
            final BaseAsyncResponseType rp = ExportOrgRegistry.generateResponse (db, exportOrgRegistryRequest);
            System.out.println (rp);
            validate (rp);
        }
        
    }
    
}
