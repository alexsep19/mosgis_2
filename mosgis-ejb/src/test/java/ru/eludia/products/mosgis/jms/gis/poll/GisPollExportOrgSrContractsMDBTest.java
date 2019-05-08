package ru.eludia.products.mosgis.jms.gis.poll;

import java.io.File;
import java.util.List;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import org.junit.Test;
import org.junit.Test.None;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;
import ru.gosuslugi.dom.schema.integration.house_management.ExportSupplyResourceContractResultType;

public class GisPollExportOrgSrContractsMDBTest extends BaseTest {
    
    GisPollExportOrgSrContractsMDB mdb = new GisPollExportOrgSrContractsMDB ();
    
    public GisPollExportOrgSrContractsMDBTest () throws Exception {
        
        jc = JAXBContext.newInstance (GetStateResult.class);
    }

    @Test (expected = None.class)
    public void test () throws Exception {

	GetStateResult getStateResult = (GetStateResult) jc.createUnmarshaller ().unmarshal (new File ("c:\\projects\\mosgis\\incoming\\tmp\\exportSrCtr1000.xml"));

	List<ExportSupplyResourceContractResultType> exportContracts = getStateResult.getExportSupplyResourceContractResult().get(0).getContract();
	try (DB db = model.getDb()) {
	    mdb.storeSrContracts(db, DB.HASH(
		"uuid_out_soap", UUID.fromString("885ce262-b543-1e73-e053-0d0b000ad87a"),
		"log.uuid_object", UUID.fromString("2953367e-113e-4cc8-b244-d52b4de73c37"),
		"log.uuid_user", UUID.fromString("79adb621-ecf3-01ec-e053-0d0b000ac65e")
		), exportContracts);
	}
    }
    
}
