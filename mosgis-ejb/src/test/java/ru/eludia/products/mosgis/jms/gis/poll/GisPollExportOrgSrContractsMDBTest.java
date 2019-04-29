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

	GetStateResult getStateResult = (GetStateResult) jc.createUnmarshaller ().unmarshal (new File ("c:\\projects\\mosgis\\incoming\\tmp\\exportSrCtr.xml"));

	List<ExportSupplyResourceContractResultType> exportContracts = getStateResult.getExportSupplyResourceContractResult().get(0).getContract();
	try (DB db = model.getDb()) {
	    mdb.storeSrContracts(db, UUID.fromString("878db72d-1191-072b-e053-0d0b000a7336"), UUID.fromString("2953367e-113e-4cc8-b244-d52b4de73c37"), exportContracts);
	}
    }
    
}
