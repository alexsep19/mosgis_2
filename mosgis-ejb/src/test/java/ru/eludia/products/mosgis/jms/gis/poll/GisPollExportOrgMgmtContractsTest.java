package ru.eludia.products.mosgis.jms.gis.poll;

import java.io.File;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import org.junit.Test;
import org.junit.Test.None;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;

public class GisPollExportOrgMgmtContractsTest extends BaseTest {
    
    private static final UUID uuidOrg = UUID.fromString ("595040e0-fbc7-4a19-8a23-e2ac97894f46");
    
    GisPollExportOrgMgmtContracts mdb = new GisPollExportOrgMgmtContracts ();
    
    public GisPollExportOrgMgmtContractsTest () throws Exception {
        
        jc = JAXBContext.newInstance (GetStateResult.class);
        
    }

    @Test (expected = None.class)
    public void test () throws Exception {
        
        GetStateResult getStateResult = (GetStateResult) jc.createUnmarshaller ().unmarshal (new File ("c:\\projects\\mosgis\\incoming\\tmp\\exportCAChResult.xml"));
        
        try (DB db = model.getDb ()) {
            
            mdb.process (db, uuidOrg, getStateResult.getExportCAChResult ());
            
        }
        
    }
    
}
