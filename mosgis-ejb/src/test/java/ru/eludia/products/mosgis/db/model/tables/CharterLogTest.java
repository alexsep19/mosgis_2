package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import org.junit.Test;
import static ru.eludia.base.DB.HASH;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.ws.soap.tools.SOAPTools;
import ru.gosuslugi.dom.schema.integration.house_management.ImportCharterRequest;

public class CharterLogTest extends BaseTest {
    
    public CharterLogTest () throws Exception {
        
        super ();
        jc = JAXBContext.newInstance (ImportCharterRequest.class);
        schema = SOAPTools.loadSchema ("house-management/hcs-house-management-types.xsd");
        
    }

    @Test (expected = Test.None.class)
    public void testAnnul () {
        
        validate (CharterLog.toAnnul (HASH (
            "reasonofannulment", "exportCAChData вернул более одного утверждённого устава: первый оставляем, остальные аннулируем",
            "ctr.charterversionguid", UUID.randomUUID ()
        )));
        
    }

}
