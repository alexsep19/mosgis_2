package ru.eludia.products.mosgis.db.model;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;

public class DataSourceImpl implements DataSource {
    
    private static final Logger logger = Logger.getLogger (DataSourceImpl.class.getName ());
    PrintWriter w = new PrintWriter (System.err);
    Connection cn;    

    public DataSourceImpl (Connection cn) {
        this.cn = cn;
    }

    @Override
    public Connection getConnection () throws SQLException {
        return new WrappedConnection (cn);
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
