package ru.eludia.products.mosgis.db.model;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.ClassRule;
import org.junit.rules.TestRule;

public class MosGisModelTest {
    
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
        m.update ();
    }
    
}