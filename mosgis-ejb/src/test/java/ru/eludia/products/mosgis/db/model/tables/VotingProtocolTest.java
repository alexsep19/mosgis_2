package ru.eludia.products.mosgis.db.model.tables;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.ClassRule;
import org.junit.rules.TestRule;
import ru.eludia.base.DB;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.DataSourceImpl;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.TestRuleImpl;

public class VotingProtocolTest {
    
    private static final Logger logger = Logger.getLogger (VotingProtocolTest.class.getName ());
    
    private static MosGisModel m;
        
    @ClassRule
    public static TestRule classRule = new TestRuleImpl ();
    private static Connection getCn () {
        return ((TestRuleImpl) classRule).getCn ();
    }
    
    @BeforeClass
    public static void setUpClass () throws Exception {
        m = new MosGisModel (new DataSourceImpl (getCn ()));
    }
    
    @AfterClass
    public static void tearDownClass () throws SQLException {
        if (getCn () != null) getCn ().close ();
    }
        
    @Test
    public void testMethod () throws SQLException {
        Table t = m.get (VotingProtocol.class);        
        Map<String, Object> h = t.randomHASH (DB.HASH ("meeting_av_place", null));
        logger.info (h.toString ());
    }
    
}
