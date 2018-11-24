package ru.eludia.products.mosgis.db.model;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.ClassRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class MosGisModelTest {
    
    private static final Logger logger = Logger.getLogger (MosGisModelTest.class.getName ());

    private static DataSource ds;
    private static MosGisModel m;
    
    public MosGisModelTest () {
    }
    
    @ClassRule
    public static TestRule classRule = new TestRuleImpl ();
    
    @BeforeClass
    public static void setUpClass () throws Exception {
        Class.forName ("oracle.jdbc.driver.OracleDriver");
        ds = new DataSourceImpl ();
        m = new MosGisModel (ds);
    }
    
    @AfterClass
    public static void tearDownClass () {
    }
    
    @Before
    public void setUp () {
    }
    
    @After
    public void tearDown () {
    }
    
    @Test
    public void testMethod () {
        
    }

    private static class DataSourceImpl implements DataSource {
        
        PrintWriter w = new PrintWriter (System.err);

        public DataSourceImpl () {
        }

        @Override
        public Connection getConnection () throws SQLException {
            return DriverManager.getConnection ("jdbc:oracle:thin:@localhost:1521:XE", "mg", "z");
        }

        @Override
        public Connection getConnection (String username, String password) throws SQLException {
            return getConnection ();
        }

        @Override
        public PrintWriter getLogWriter () throws SQLException {
            return w;
        }

        @Override
        public void setLogWriter (PrintWriter out) throws SQLException {
        }

        @Override
        public void setLoginTimeout (int seconds) throws SQLException {
        }

        @Override
        public int getLoginTimeout () throws SQLException {
            return 1000;
        }

        @Override
        public Logger getParentLogger () throws SQLFeatureNotSupportedException {
            return logger;
        }

        @Override
        public <T> T unwrap (Class<T> iface) throws SQLException {
            return null;
        }

        @Override
        public boolean isWrapperFor (Class<?> iface) throws SQLException {
            return false;
        }
    }

    private static class TestRuleImpl implements TestRule {

        public TestRuleImpl () {
        }

        @Override
        public Statement apply (Statement stmnt, Description d) {
            
            try {
                Class.forName ("oracle.jdbc.driver.OracleDriver");
            }
            catch (ClassNotFoundException ex) {
                return new Croak ("Oracle JDBC driver not found");
            }
            
            return stmnt;
            
        }

        private static class Croak extends Statement {
            
            String s;
            
            public Croak (String s) {
                this.s = s;
            }
            
            @Override
            public void evaluate () throws Throwable {
                logger.info ("Test skipped: " + s);
            }
            
        }
        
    }
    
}
