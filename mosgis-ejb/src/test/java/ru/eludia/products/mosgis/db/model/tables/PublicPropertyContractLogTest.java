package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import javax.xml.bind.JAXBContext;
import org.junit.Test;
import static ru.eludia.base.DB.HASH;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.tables.base.BaseTest;
import ru.eludia.products.mosgis.ws.base.AbstactServiceAsync;
import ru.gosuslugi.dom.schema.integration.house_management.ImportPublicPropertyContractRequest;

public class PublicPropertyContractLogTest extends BaseTest {
    
    private Table table;
    private Map<String, Object> commonPart = HASH (
        "id_log", null
    );
    
    public PublicPropertyContractLogTest () throws Exception {
        super ();
        jc     = JAXBContext.newInstance (ImportPublicPropertyContractRequest.class);
        schema = AbstactServiceAsync.loadSchema ("house-management/hcs-house-management-types.xsd");
        table  = model.get (PublicPropertyContract.class);
    }
    
    @Test
    public void test () {        
        Table.Sampler sampler = table.new Sampler (commonPart);        
        Map<String, Object> sample = sampler.nextHASH ();        
        for (int k = 0; k < sampler.getCount (); k ++) check (sampler.cutOut (sample, k));        
    }

    private void check (final Map<String, Object> r) throws IllegalStateException {
        dump (r);        
        validate (PublicPropertyContractLog.toImportPublicPropertyContractRequest (r));
    }
        
}
