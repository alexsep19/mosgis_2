package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import org.junit.Ignore;
import org.junit.Test;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.ws.soap.tools.SOAPTools;
import ru.gosuslugi.dom.schema.integration.uk.ImportDocumentMunicipalRequest;
import ru.gosuslugi.dom.schema.integration.uk.ImportDocumentRegionRequest;

public class LegalActLogTest extends BaseTest {
    
    LegalAct table;
    LegalActLog logTable;
    
    public LegalActLogTest () throws Exception {
        
        super ();
        jc = JAXBContext.newInstance (ImportDocumentRegionRequest.class);
        schema = SOAPTools.loadSchema ("uk/hcs-uk-types.xsd");
        
        table = (LegalAct) model.get (LegalAct.class);
        logTable = (LegalActLog) model.get (LegalActLog.class);
        
    }

    private Map<String, Object> getData() throws SQLException {

	try (DB db = model.getDb()) {
	    final Map<String, Object> r = logTable.getForExport(db, "8481f9cb-91f9-0bba-e053-0d0b000a358d");
	    r.put("attachmentguid", r.get("uuid"));
	    r.put("attachmenthash", "0000000000000000");
	    return r;
	}
    }

    @Test (expected = Test.None.class)
    public void testInsertRegional () throws SQLException {
        
        Map<String, Object> r = getData ();
        
        r.put ("documentguid", null);
        dump (r);        
        validate (LegalActLog.toImportDocumentsRegionRequest (r));
        
    }
    
    @Test (expected = Test.None.class)
    public void testUpdateRegional () throws SQLException {
        
        Map<String, Object> r = getData ();
                
        r.put ("documentguid", r.get ("uuid"));
        dump (r);        
        validate (LegalActLog.toImportDocumentsRegionRequest (r));
    }
}