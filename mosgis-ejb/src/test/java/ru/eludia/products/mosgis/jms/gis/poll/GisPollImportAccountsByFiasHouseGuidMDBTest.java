package ru.eludia.products.mosgis.jms.gis.poll;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import org.w3c.dom.Node;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.eludia.products.mosgis.ws.soap.tools.SOAPTools;
import ru.gosuslugi.dom.schema.integration.house_management.ExportAccountResultType;
import ru.gosuslugi.dom.schema.integration.house_management.GetStateResult;

public class GisPollImportAccountsByFiasHouseGuidMDBTest extends BaseTest {
    
    UUID uuid = UUID.fromString ("157fc9e7-7f9e-44d6-b130-1c1891fc2ed7");
    
    GisPollImportAccountsByFiasHouseGuidMDB mdb = new GisPollImportAccountsByFiasHouseGuidMDB ();            
    
    public GisPollImportAccountsByFiasHouseGuidMDBTest () throws Exception {
        
        jc = JAXBContext.newInstance (GetStateResult.class);
    }

    @Test (expected = Test.None.class)
    public void test () throws Exception {
        
        try (DB db = model.getDb ()) {
                                                            
            GetStateResult state = (GetStateResult) jc.createUnmarshaller ().unmarshal (SOAPTools.getSoapBodyNode (db.getString (OutSoap.class, uuid, "rp")));
                        
            for (ExportAccountResultType i: state.getExportAccountResult ()) mdb.store (db, i);            
            
        }        
        
    }
    
}
