package ru.eludia.products.mosgis.db.model;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class WrappedConnection implements Connection {
    
    Connection cn;    

    public WrappedConnection (Connection cn) {
        this.cn = cn;
    }
    
    @Override
    public java.sql.Statement createStatement () throws SQLException {
        return cn.createStatement ();
    }

    @Override
    public PreparedStatement prepareStatement (String sql) throws SQLException {
        return cn.prepareStatement (sql);
    }

    @Override
    public CallableStatement prepareCall (String sql) throws SQLException {
        return cn.prepareCall (sql);
    }

    @Override
    public String nativeSQL (String sql) throws SQLException {
        return cn.nativeSQL (sql);
    }

    @Override
    public void setAutoCommit (boolean autoCommit) throws SQLException {
        cn.setAutoCommit (autoCommit);
    }

    @Override
    public boolean getAutoCommit () throws SQLException {
        return cn.getAutoCommit ();
    }

    @Override
    public void commit () throws SQLException {
        cn.commit ();
    }

    @Override
    public void rollback () throws SQLException {
        cn.rollback ();
    }

    @Override
    public void close () throws SQLException {
        // do nothing
    }

    @Override
    public boolean isClosed () throws SQLException {
        return cn.isClosed ();
    }

    @Override
    public DatabaseMetaData getMetaData () throws SQLException {
        return cn.getMetaData ();
    }

    @Override
    public void setReadOnly (boolean readOnly) throws SQLException {
        cn.setReadOnly (readOnly);
    }

    @Override
    public boolean isReadOnly () throws SQLException {
        return cn.isReadOnly ();
    }

    @Override
    public void setCatalog (String catalog) throws SQLException {
        cn.setCatalog (catalog);
    }

    @Override
    public String getCatalog () throws SQLException {
        return cn.getCatalog ();
    }

    @Override
    public void setTransactionIsolation (int level) throws SQLException {
        cn.setTransactionIsolation (level);
    }

    @Override
    public int getTransactionIsolation () throws SQLException {
        return cn.getTransactionIsolation ();
    }

    @Override
    public SQLWarning getWarnings () throws SQLException {
        return cn.getWarnings ();
    }

    @Override
    public void clearWarnings () throws SQLException {
        cn.clearWarnings ();
    }

    @Override
    public java.sql.Statement createStatement (int resultSetType, int resultSetConcurrency) throws SQLException {
        return cn.createStatement (resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement (String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return cn.prepareStatement (sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall (String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return cn.prepareCall (sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public Map<String, Class<?>> getTypeMap () throws SQLException {
        return cn.getTypeMap ();
    }

    @Override
    public void setTypeMap (Map<String, Class<?>> map) throws SQLException {
        cn.setTypeMap (map);
    }

    @Override
    public void setHoldability (int holdability) throws SQLException {
        cn.setHoldability (holdability);
    }

    @Override
    public int getHoldability () throws SQLException {
        return cn.getHoldability ();
    }

    @Override
    public Savepoint setSavepoint () throws SQLException {
        return cn.setSavepoint ();
    }

    @Override
    public Savepoint setSavepoint (String name) throws SQLException {
        return cn.setSavepoint (name);
    }

    @Override
    public void rollback (Savepoint savepoint) throws SQLException {
        cn.rollback (savepoint);
    }

    @Override
    public void releaseSavepoint (Savepoint savepoint) throws SQLException {
        cn.releaseSavepoint (savepoint);
    }

    @Override
    public java.sql.Statement createStatement (int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return cn.createStatement (resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement (String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return cn.prepareStatement (sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public CallableStatement prepareCall (String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return cn.prepareCall (sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement (String sql, int autoGeneratedKeys) throws SQLException {
        return cn.prepareStatement (sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement (String sql, int[] columnIndexes) throws SQLException {
        return cn.prepareStatement (sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement (String sql, String[] columnNames) throws SQLException {
        return cn.prepareStatement (sql, columnNames);
    }

    @Override
    public Clob createClob () throws SQLException {
        return cn.createClob ();
    }

    @Override
    public Blob createBlob () throws SQLException {
        return cn.createBlob ();
    }

    @Override
    public NClob createNClob () throws SQLException {
        return cn.createNClob ();
    }

    @Override
    public SQLXML createSQLXML () throws SQLException {
        return cn.createSQLXML ();
    }

    @Override
    public boolean isValid (int timeout) throws SQLException {
        return cn.isValid (timeout);
    }

    @Override
    public void setClientInfo (String name, String value) throws SQLClientInfoException {
        cn.setClientInfo (name, value);
    }

    @Override
    public void setClientInfo (Properties properties) throws SQLClientInfoException {
        cn.setClientInfo (properties);
    }

    @Override
    public String getClientInfo (String name) throws SQLException {
        return cn.getClientInfo (name);
    }

    @Override
    public Properties getClientInfo () throws SQLException {
        return cn.getClientInfo ();
    }

    @Override
    public Array createArrayOf (String typeName, Object[] elements) throws SQLException {
        return cn.createArrayOf (typeName, elements);
    }

    @Override
    public Struct createStruct (String typeName, Object[] attributes) throws SQLException {
        return cn.createStruct (typeName, attributes);
    }

    @Override
    public void setSchema (String schema) throws SQLException {
        cn.setSchema (schema);
    }

    @Override
    public String getSchema () throws SQLException {
        return cn.getSchema ();
    }

    @Override
    public void abort (Executor executor) throws SQLException {
        cn.abort (executor);
    }

    @Override
    public void setNetworkTimeout (Executor executor, int milliseconds) throws SQLException {
        cn.setNetworkTimeout (executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout () throws SQLException {
        return cn.getNetworkTimeout ();
    }

    @Override
    public <T> T unwrap (Class<T> iface) throws SQLException {
        return cn.unwrap (iface);
    }

    @Override
    public boolean isWrapperFor (Class<?> iface) throws SQLException {
        return cn.isWrapperFor (iface);
    }
    
}
