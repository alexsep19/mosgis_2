package ru.eludia.products.mosgis.jms.gis.poll;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.xml.bind.JAXBContext;
import org.junit.Test;
import org.junit.Test.None;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.gosuslugi.dom.schema.integration.uk.ExportDocumentType;
import ru.gosuslugi.dom.schema.integration.uk.GetStateResult;

public class GisPollImportLegalActsTest extends BaseTest {
    
    GisPollImportLegalActs mdb = new GisPollImportLegalActs ();
    
    public GisPollImportLegalActsTest () throws Exception {
        
        jc = JAXBContext.newInstance (GetStateResult.class);
    }

    @Test (expected = None.class)
    public void test () throws Exception {

	GetStateResult getStateResult = (GetStateResult) jc.createUnmarshaller ().unmarshal (new File ("c:\\code\\mosgis\\incoming\\tmp\\exportDocuments.xml"));

	List<ExportDocumentType> exportDocumentsResult = getStateResult.getDocument();
	try (DB db = model.getDb()) {
	    mdb.storeLegalActs (db, UUID.fromString("84a56709-9270-08b2-e053-0d0b000a529a"), exportDocumentsResult);
	}
    }
    
}
