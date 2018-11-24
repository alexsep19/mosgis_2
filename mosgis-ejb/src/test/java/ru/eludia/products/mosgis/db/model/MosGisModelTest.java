package ru.eludia.products.mosgis.db.model;

import java.io.File;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Locale;
import java.util.Properties;
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

    private static Connection cn;
    private static DataSource ds;
    private static MosGisModel m;
    
    public MosGisModelTest () {
    }
    
    @ClassRule
    public static TestRule classRule = new TestRuleImpl ();
    
    @BeforeClass
    public static void setUpClass () throws Exception {
        ds = new DataSourceImpl ();
        m = new MosGisModel (ds);
    }
    
    @AfterClass
    public static void tearDownClass () throws SQLException {
        if (cn != null) cn.close ();
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
            return cn;
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
            
            String path = System.getProperty (RU_ELUDIA_DB_DRIVER_PATH);
            
            if (path == null || path.isEmpty ()) return new Croak (RU_ELUDIA_DB_DRIVER_PATH + " not set, skipping test");
            
            File file = new File (path);
            if (!file.exists ()) return new Croak ("File not found: " + file);
            
            URL [] urls = new URL [1];
            try {
                urls [0] = file.toURI ().toURL ();
            }
            catch (MalformedURLException ex) {
                return new Croak (ex);
            }
                        
            URLClassLoader loader = new URLClassLoader (urls, stmnt.getClass ().getClassLoader ());
            
            try {
                Class driverClass = Class.forName ("oracle.jdbc.driver.OracleDriver", true, loader);
                Driver driver = (Driver) driverClass.newInstance ();
                final Properties p = new Properties ();
                p.put ("user", System.getProperty (RU_ELUDIA_DB_USER));
                p.put ("password", System.getProperty (RU_ELUDIA_DB_PASSWORD));
                Locale.setDefault (Locale.US);
                cn = driver.connect (System.getProperty (RU_ELUDIA_DB_URL), p);
            }
            catch (Exception ex) {
                return new Croak (ex);
            }
            
            return stmnt;
            
        }
        
        private static final String RU_ELUDIA_DB_DRIVER_PATH = "ru.eludia.db.driver.path";
        private static final String RU_ELUDIA_DB_URL = "ru.eludia.db.url";
        private static final String RU_ELUDIA_DB_USER = "ru.eludia.db.user";
        private static final String RU_ELUDIA_DB_PASSWORD = "ru.eludia.db.password";

        private static class Croak extends Statement {
            
            String s;
            Throwable t = null;
            
            public Croak (String s) {
                this.s = s;
            }
            
            public Croak (Throwable t) {
                this.t = t;
                this.s = t.getMessage ();
            }
            
            @Override
            public void evaluate () throws Throwable {
                if (t != null) logger.log (Level.SEVERE, s, t); else logger.info ("Test skipped: " + s);
                logger.info ("System.getProperties () = " + System.getProperties ());
            }
            
        }
        
    }
    
}
