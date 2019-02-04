package ru.eludia.products.mosgis.db.model.tables;

import java.sql.SQLException;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import org.junit.Ignore;
import org.junit.Test;
import ru.eludia.base.DB;
import static ru.eludia.base.DB.HASH;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.ws.base.AbstactServiceAsync;
import ru.gosuslugi.dom.schema.integration.infrastructure.ImportOKIRequest;
import ru.gosuslugi.dom.schema.integration.infrastructure.ExportOKIRequest;

public class InfrastructureLogTest extends BaseTest {
    
    private static final String uuid = "3145162D2CE348DD889A8D6987C3FA35";
    
    private Infrastructure table;
    private InfrastructureLog logTable;
    
    public InfrastructureLogTest() throws Exception {
        
        super ();
        
        jc            = JAXBContext.newInstance (ImportOKIRequest.class, ExportOKIRequest.class);
        schema        = AbstactServiceAsync.loadSchema ("infrastructure/hcs-infrastructure-types.xsd");
        
        table = (Infrastructure) model.get (Infrastructure.class);
        logTable = (InfrastructureLog) model.get(InfrastructureLog.class);
        
    }
    
    private void checkSample (Map<String, Object> rr) throws SQLException {
        
        try (DB db = model.getDb ()) {
            
            Map<String, Object> record = 
                    db.getMap (logTable.getForExport(uuid));
            
            System.out.println (record);
            
            checkImport (record);
            
        }
        
    }
    
    private void checkImport (final Map<String, Object> r) throws IllegalStateException {
        dump (r);
        validate (InfrastructureLog.toImportOKIRequest (r));
    }
    
 //   @Ignore
    @Test
    public void testInsert () throws SQLException {

        checkSample (HASH (
        ));        

    }
    
}
