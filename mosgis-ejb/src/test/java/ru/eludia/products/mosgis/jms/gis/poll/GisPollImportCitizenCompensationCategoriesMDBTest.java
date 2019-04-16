package ru.eludia.products.mosgis.jms.gis.poll;

import java.io.File;
import java.util.List;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import org.junit.Test;
import org.junit.Test.None;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.gosuslugi.dom.schema.integration.msp.ExportCategoryType;
import ru.gosuslugi.dom.schema.integration.msp.GetStateResult;

public class GisPollImportCitizenCompensationCategoriesMDBTest extends BaseTest {
    
    GisPollImportCitizenCompensationCategoriesMDB mdb = new GisPollImportCitizenCompensationCategoriesMDB ();
    
    public GisPollImportCitizenCompensationCategoriesMDBTest () throws Exception {
        
        jc = JAXBContext.newInstance (GetStateResult.class);
    }

    @Test (expected = None.class)
    public void test () throws Exception {

	GetStateResult getStateResult = (GetStateResult) jc.createUnmarshaller ().unmarshal (new File ("c:\\projects\\mosgis\\incoming\\tmp\\exportCitizenCategories.xml"));

	List<ExportCategoryType> exportCategories = getStateResult.getCategory();
	try (DB db = model.getDb()) {
	    mdb.storeCitizenCompensationCategories (db, UUID.fromString("84a56709-9270-08b2-e053-0d0b000a529a"), exportCategories);
	}
    }
    
}
