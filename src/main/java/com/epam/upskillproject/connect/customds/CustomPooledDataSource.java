package com.epam.upskillproject.connect.customds;

import com.epam.upskillproject.init.PropertiesKeeper;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hsqldb.jdbc.JDBCDataSource;
import java.sql.*;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Singleton(name = "customProjectDB")
public class CustomPooledDataSource extends JDBCDataSource {

    private static final Logger logger = LogManager.getLogger(CustomPooledDataSource.class.getName());

    // Database an ConnectionPool properties
    private static final String DB_URL_PROP = "db.url";
    private static final String DB_NAME_PROP = "db.name";
    private static final String DB_PASSWORD_PROP = "db.password";
    private static final String CP_MIN_CONNECT_PROP = "cp.minConnectionsNumber";
    private static final String CP_MAX_CONNECT_PROP = "cp.maxConnectionsNumber";
    private static final String CP_REQUEST_TIMEOUT_VALUE_PROP = "cp.requestTimeoutValue";
    private static final String CP_REQUEST_TIMEOUT_UNIT_PROP = "cp.requestTimeoutUnit";
    private static final String CP_INACTIVITY_LIMIT_MS_PROP = "cp.inactivityTimeLimitMillis";
    // ConnectionPool default values
    private static final int DEFAULT_CP_MIN_CONNECT = 5;
    private static final int DEFAULT_CP_MAX_CONNECT = 10;
    private static final int DEFAULT_CP_REQUEST_TIMEOUT_VALUE = 5;
    private static final TimeUnit DEFAULT_CP_REQUEST_TIMEOUT_UNIT = TimeUnit.SECONDS;
    private static final int DEFAULT_CP_INACTIVITY_LIMIT_MS = 100000;

    private PropertiesKeeper propertiesKeeper;

    private String url;
    private String name;
    private String password;
    private int minConnectionsNumber;
    private int maxConnectionsNumber;
    private int requestTimeoutValue;
    private TimeUnit requestTimeoutUnit;
    private int inactivityTimeLimitMillis;

    private BlockingQueue<PoolConnection> connectionPool;
    private int connectionsInUse = 0;

    @Inject
    public CustomPooledDataSource(PropertiesKeeper propertiesKeeper) {
        this.propertiesKeeper = propertiesKeeper;
    }

    @PostConstruct
    private void init() {
        url = propertiesKeeper.getString(DB_URL_PROP);
        name = propertiesKeeper.getString(DB_NAME_PROP);
        password = propertiesKeeper.getString(DB_PASSWORD_PROP);
        minConnectionsNumber = propertiesKeeper.getIntOrDefault(CP_MIN_CONNECT_PROP, DEFAULT_CP_MIN_CONNECT);
        maxConnectionsNumber = propertiesKeeper.getIntOrDefault(CP_MAX_CONNECT_PROP, DEFAULT_CP_MAX_CONNECT);
        requestTimeoutValue = propertiesKeeper.getIntOrDefault(CP_REQUEST_TIMEOUT_VALUE_PROP,
                DEFAULT_CP_REQUEST_TIMEOUT_VALUE);
        requestTimeoutUnit = propertiesKeeper.getTimeUnitOrDefault(CP_REQUEST_TIMEOUT_UNIT_PROP,
                DEFAULT_CP_REQUEST_TIMEOUT_UNIT);
        inactivityTimeLimitMillis = propertiesKeeper.getIntOrDefault(CP_INACTIVITY_LIMIT_MS_PROP,
                DEFAULT_CP_INACTIVITY_LIMIT_MS);

        connectionPool = new ArrayBlockingQueue<>(maxConnectionsNumber);
        while (totalConnections() < minConnectionsNumber) {
            try {
                connectionPool.add(createConnection());
            } catch (SQLException e) {
                logger.log(Level.WARN, "Exception thrown during Connection pool initialization", e);
            }
        }
        logger.log(Level.DEBUG, String.format("Connection pool has been initialized (total connections %d)",
                totalConnections()));
    }

    public void setMinConnectionsNumber(int minConnectionsNumber) throws IllegalArgumentException {
        if (minConnectionsNumber >= 1 && minConnectionsNumber <= this.maxConnectionsNumber) {
            this.minConnectionsNumber = minConnectionsNumber;
            logger.log(Level.TRACE, String.format("Connection pool minConnectionsNumber has been changed to %d",
                    minConnectionsNumber));
        } else {
            logger.log(Level.WARN, String.format("Invalid minConnectionsNumber passed - %d (allowed values [1...%d])",
                    minConnectionsNumber, this.maxConnectionsNumber));
            throw new IllegalArgumentException("Invalid min pool connections number (the number must be less than " +
                    "the max pool connections number and greater than zero)");
        }
    }

    public void setMaxConnectionsNumber(int maxConnectionsNumber) throws IllegalArgumentException {
        if (maxConnectionsNumber >= 1 && this.minConnectionsNumber <= maxConnectionsNumber) {
            this.maxConnectionsNumber = maxConnectionsNumber;
            logger.log(Level.TRACE, String.format("Connection pool maxConnectionsNumber has been changed to %d",
                    maxConnectionsNumber));
        } else {
            logger.log(Level.WARN, String.format("Invalid maxConnectionsNumber passed - %d (allowed values greater " +
                            "or equal than %d)", maxConnectionsNumber, this.minConnectionsNumber));

            throw new IllegalArgumentException("Invalid max pool connections number (the number must be greater or " +
                    "equal than the min pool connections number and greater then zero)");
        }
    }

    public void setRequestTimeoutValue(int requestTimeoutValue) throws IllegalArgumentException {
        if (requestTimeoutValue > 0) {
            this.requestTimeoutValue = requestTimeoutValue;
            logger.log(Level.TRACE, String.format("Connection pool requestTimeoutValue has been changed to %d",
                    requestTimeoutValue));
        } else {
            logger.log(Level.WARN, String.format("Invalid requestTimeoutValue passed - %d (allowed values greater " +
                    "than zero)", requestTimeoutValue));
            throw new IllegalArgumentException("Invalid request timeout value (the value must be greater than zero)");
        }
    }

    public void setRequestTimeoutUnit(TimeUnit requestTimeoutUnit) throws IllegalArgumentException {
        if (requestTimeoutUnit != null) {
            this.requestTimeoutUnit = requestTimeoutUnit;
            logger.log(Level.TRACE, String.format("Connection pool requestTimeoutUnit has been changed to %s",
                    requestTimeoutUnit.toString()));
        } else {
            logger.log(Level.WARN, "Null value of requestTimeoutUnit passed");
            throw new IllegalArgumentException("Request timeout unit cannot be null");
        }
    }

    public void setInactivityTimeLimitMillis(int inactivityTimeLimitMillis) throws IllegalArgumentException {
        if (inactivityTimeLimitMillis > 0) {
            this.inactivityTimeLimitMillis = inactivityTimeLimitMillis;
            logger.log(Level.TRACE, String.format("Connection pool inactivityTimeLimitMillis has been changed to %d",
                    inactivityTimeLimitMillis));
        } else {
            logger.log(Level.WARN, String.format("Invalid inactivityTimeLimitMillis passed - %d (allowed values " +
                    "greater than zero)", inactivityTimeLimitMillis));
            throw new IllegalArgumentException("Invalid inactivity time limit value (the value must be greater than " +
                    "zero)");
        }
    }

    @Override
    public synchronized Connection getConnection() throws SQLException {
        Connection connection = null;
        if (connectionPool.isEmpty() && totalConnections() < maxConnectionsNumber) {
            connection = createConnection();
            logger.log(Level.TRACE, "New connection created");
        } else {
            try {
                connection = connectionPool.poll(requestTimeoutValue, requestTimeoutUnit);
                logger.log(Level.TRACE, "Connection obtained from connection pool");
            } catch (InterruptedException e) {
                logger.log(Level.WARN, "Cannot get connection from Connection pool, new connection will be " +
                        "created", e);
            }
        }
        connectionsInUse++;
        logger.log(Level.TRACE, String.format("Number of connections in use: %d", connectionsInUse));
        return (connection != null) ? connection : createConnection();
    }

    // Method is not supported
    @Override
    public Connection getConnection(String username, String password) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported by CustomPooledDataSource");
    }

    private PoolConnection createConnection() throws SQLException {
        return new PoolConnection(DriverManager.getConnection(url, user, password));
    }

    private int totalConnections() {
        return this.connectionPool.size() + connectionsInUse;
    }

    private void releaseConnection(PoolConnection poolConnection) {
        try {
            closeOldConnections();
            if (connectionPool.size() < maxConnectionsNumber) {
                poolConnection.setAutoCommit(true);
                connectionPool.add(poolConnection);
                logger.log(Level.TRACE, "Connection was returned to Connection pool");
            } else {
                poolConnection.shutdown();
                logger.log(Level.TRACE, "Connection was closed");
            }
            connectionsInUse--;
            logger.log(Level.TRACE, String.format("Number of connections in use: %d", connectionsInUse));
        } catch (SQLException e) {
            logger.log(Level.WARN, "Cannot release connection", e);
        }
    }

    private void closeOldConnections() throws SQLException {
        int closedConnections = 0;
        while (connectionPool.size() >= minConnectionsNumber) {
            Optional<PoolConnection> oldConnection = connectionPool.stream().filter(c ->
                    System.currentTimeMillis() - c.getLastUsageTimeStamp() > inactivityTimeLimitMillis).findAny();
            if (oldConnection.isPresent()) {
                if (connectionPool.remove(oldConnection.get())) {
                    oldConnection.get().shutdown();
                    closedConnections++;
                }
            } else {
                break;
            }
        }
        if (closedConnections > 0) {
            logger.log(Level.TRACE, String.format("%d old connections from Connection pool were closed",
                    closedConnections));
        }
    }

    private class PoolConnection implements Connection {
        private Connection connection;
        private long lastUsageTimeStamp = System.currentTimeMillis();

        public PoolConnection(Connection connection) {
            this.connection = connection;
        }

        private void shutdown() throws SQLException {
            connection.close();
        }

        public long getLastUsageTimeStamp() {
            return lastUsageTimeStamp;
        }

        @Override
        public void close() {
            releaseConnection(this);
        }

        @Override
        public void beginRequest() throws SQLException {
            connection.beginRequest();
        }

        @Override
        public void endRequest() throws SQLException {
            connection.endRequest();
        }

        @Override
        public boolean setShardingKeyIfValid(ShardingKey shardingKey, ShardingKey superShardingKey, int timeout) throws
                SQLException {
            return connection.setShardingKeyIfValid(shardingKey, superShardingKey, timeout);
        }

        @Override
        public boolean setShardingKeyIfValid(ShardingKey shardingKey, int timeout) throws SQLException {
            return connection.setShardingKeyIfValid(shardingKey, timeout);
        }

        @Override
        public void setShardingKey(ShardingKey shardingKey, ShardingKey superShardingKey) throws SQLException {
            connection.setShardingKey(shardingKey, superShardingKey);
        }

        @Override
        public void setShardingKey(ShardingKey shardingKey) throws SQLException {
            connection.setShardingKey(shardingKey);
        }

        @Override
        public int hashCode() {
            return connection.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return connection.equals(obj);
        }

        @Override
        public String toString() {
            return connection.toString();
        }

        @Override
        public Statement createStatement() throws SQLException {
            return connection.createStatement();
        }

        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException {
            return connection.prepareStatement(sql);
        }

        @Override
        public CallableStatement prepareCall(String sql) throws SQLException {
            return connection.prepareCall(sql);
        }

        @Override
        public String nativeSQL(String sql) throws SQLException {
            return connection.nativeSQL(sql);
        }

        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException {
            connection.setAutoCommit(autoCommit);
        }

        @Override
        public boolean getAutoCommit() throws SQLException {
            return connection.getAutoCommit();
        }

        @Override
        public void commit() throws SQLException {
            connection.commit();
        }

        @Override
        public void rollback() throws SQLException {
            connection.rollback();
        }


        @Override
        public boolean isClosed() throws SQLException {
            return connection.isClosed();
        }

        @Override
        public DatabaseMetaData getMetaData() throws SQLException {
            return connection.getMetaData();
        }

        @Override
        public void setReadOnly(boolean readOnly) throws SQLException {
            connection.setReadOnly(readOnly);
        }

        @Override
        public boolean isReadOnly() throws SQLException {
            return connection.isReadOnly();
        }

        @Override
        public void setCatalog(String catalog) throws SQLException {
            connection.setCatalog(catalog);
        }

        @Override
        public String getCatalog() throws SQLException {
            return connection.getCatalog();
        }

        @Override
        public void setTransactionIsolation(int level) throws SQLException {
            connection.setTransactionIsolation(level);
        }

        @Override
        public int getTransactionIsolation() throws SQLException {
            return connection.getTransactionIsolation();
        }

        @Override
        public SQLWarning getWarnings() throws SQLException {
            return connection.getWarnings();
        }

        @Override
        public void clearWarnings() throws SQLException {
            connection.clearWarnings();
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            return connection.createStatement(resultSetType, resultSetConcurrency);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
                throws SQLException {
            return connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
                throws SQLException {
            return connection.prepareCall(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public Map<String, Class<?>> getTypeMap() throws SQLException {
            return connection.getTypeMap();
        }

        @Override
        public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
            connection.setTypeMap(map);
        }

        @Override
        public void setHoldability(int holdability) throws SQLException {
            connection.setHoldability(holdability);
        }

        @Override
        public int getHoldability() throws SQLException {
            return connection.getHoldability();
        }

        @Override
        public Savepoint setSavepoint() throws SQLException {
            return connection.setSavepoint();
        }

        @Override
        public Savepoint setSavepoint(String name) throws SQLException {
            return connection.setSavepoint(name);
        }

        @Override
        public void rollback(Savepoint savepoint) throws SQLException {
            connection.rollback(savepoint);
        }

        @Override
        public void releaseSavepoint(Savepoint savepoint) throws SQLException {
            connection.releaseSavepoint(savepoint);
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
                throws SQLException {
            return connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
                                                  int resultSetHoldability) throws SQLException {
            return connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
                                             int resultSetHoldability) throws SQLException {
            return connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            return connection.prepareStatement(sql, autoGeneratedKeys);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
            return connection.prepareStatement(sql, columnIndexes);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
            return connection.prepareStatement(sql, columnNames);
        }

        @Override
        public Clob createClob() throws SQLException {
            return connection.createClob();
        }

        @Override
        public Blob createBlob() throws SQLException {
            return connection.createBlob();
        }

        @Override
        public NClob createNClob() throws SQLException {
            return connection.createNClob();
        }

        @Override
        public SQLXML createSQLXML() throws SQLException {
            return connection.createSQLXML();
        }

        @Override
        public boolean isValid(int timeout) throws SQLException {
            return connection.isValid(timeout);
        }

        @Override
        public void setClientInfo(String name, String value) throws SQLClientInfoException {
            connection.setClientInfo(name, value);
        }

        @Override
        public void setClientInfo(Properties properties) throws SQLClientInfoException {
            connection.setClientInfo(properties);
        }

        @Override
        public String getClientInfo(String name) throws SQLException {
            return connection.getClientInfo(name);
        }

        @Override
        public Properties getClientInfo() throws SQLException {
            return connection.getClientInfo();
        }

        @Override
        public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
            return connection.createArrayOf(typeName, elements);
        }

        @Override
        public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
            return connection.createStruct(typeName, attributes);
        }

        @Override
        public void setSchema(String schema) throws SQLException {
            connection.setSchema(schema);
        }

        @Override
        public String getSchema() throws SQLException {
            return connection.getSchema();
        }

        @Override
        public void abort(Executor executor) throws SQLException {
            connection.abort(executor);
        }

        @Override
        public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
            connection.setNetworkTimeout(executor, milliseconds);
        }

        @Override
        public int getNetworkTimeout() throws SQLException {
            return connection.getNetworkTimeout();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return connection.unwrap(iface);
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return connection.isWrapperFor(iface);
        }
    }


}