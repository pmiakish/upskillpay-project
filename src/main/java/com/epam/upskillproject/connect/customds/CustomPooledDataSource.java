package com.epam.upskillproject.connect.customds;

import com.epam.upskillproject.exceptions.CustomSQLCode;
import com.epam.upskillproject.util.init.PropertiesKeeper;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hsqldb.jdbc.JDBCDataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private static final int VALIDATION_TIMEOUT_VALUE_SEC = 3;
    private static final int DEFAULT_CP_MIN_CONNECT = 5;
    private static final int DEFAULT_CP_MAX_CONNECT = 10;
    private static final int DEFAULT_CP_REQUEST_TIMEOUT_VALUE = 5;
    private static final TimeUnit DEFAULT_CP_REQUEST_TIMEOUT_UNIT = TimeUnit.SECONDS;
    private static final int DEFAULT_CP_INACTIVITY_LIMIT_MS = 100000;

    private static final String CONNECTION_FAILURE_SQLSTATE = "08001";

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
    private ArrayList<PoolConnection> activeConnections;

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
        activeConnections = new ArrayList<>();
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
        if (minConnectionsNumber > 0 && minConnectionsNumber <= this.maxConnectionsNumber) {
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
        if (maxConnectionsNumber > 0 && this.minConnectionsNumber <= maxConnectionsNumber) {
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
        if (connectionPool == null || activeConnections == null) {
            throw new IllegalStateException("Connection pool is shut down");
        }
        try {
            PoolConnection connection = connectionPool.poll(requestTimeoutValue, requestTimeoutUnit);
            logger.log(Level.TRACE, "Connection obtained from connection pool");
            if (connection == null) {
                connection = createConnection();
            } else if (!connection.isValid(VALIDATION_TIMEOUT_VALUE_SEC)) {
                connection.close();
                connection = createConnection();
            }
            activeConnections.add(connection);
            return connection;
        } catch (InterruptedException e) {
            logger.log(Level.WARN, String.format("Cannot get connection from Connection pool, timeout: %s %s",
                    requestTimeoutValue, requestTimeoutUnit.name()), e);
            throw new SQLException("Cannot get connection: connection pool exhausted", CONNECTION_FAILURE_SQLSTATE,
                    CustomSQLCode.POOL_EXHAUSTED.getCode(), e);
        }
    }

    // Method is not supported
    @Override
    public Connection getConnection(String username, String password) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported by CustomPooledDataSource");
    }

    public void shutdown() throws SQLException {
        for (Connection connection : activeConnections) {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }
        for (PoolConnection poolConnection : connectionPool) {
            if (poolConnection != null && !poolConnection.isClosed()) {
                poolConnection.shutdown();
            }
        }
        activeConnections = null;
        connectionPool = null;
    }

    public boolean isValid() {
        return (connectionPool != null && activeConnections != null && totalConnections() > 0);
    }

    private PoolConnection createConnection() throws SQLException {
        return new PoolConnection(DriverManager.getConnection(url, name, password));
    }

    private int totalConnections() {
        return this.connectionPool.size() + activeConnections.size();
    }

    private synchronized void releaseConnection(PoolConnection poolConnection) {
        if (poolConnection == null) {
            return;
        }
        try {
            boolean added = false;
            poolConnection.closeStatements();
            // in case if the connection comes from activeConnections
            if (activeConnections.remove(poolConnection)) {
                if (poolConnection.isValid(VALIDATION_TIMEOUT_VALUE_SEC) &&
                        connectionPool.size() < connectionPool.remainingCapacity()) {
                    poolConnection.setAutoCommit(true);
                    added = connectionPool.add(poolConnection);
                } else {
                    poolConnection.shutdown();
                    logger.log(Level.TRACE, "Connection from pool closed (is invalid)");
                }
            } else {
                poolConnection.shutdown();
                logger.log(Level.INFO, "Unknown connection closed");
            }
            if (added) {
                logger.log(Level.TRACE, "Connection returned to connection pool");
            }
        } catch (SQLException e) {
            logger.log(Level.WARN, "Connection was not released properly", e);
        }
        try {
            closeOldConnections();
        } catch (SQLException e) {
            logger.log(Level.WARN, "Cannot clear old connections in connection pool", e);
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
        private List<Statement> statements = new ArrayList<>();
        private List<PreparedStatement> preparedStatements = new ArrayList<>();
        private List<CallableStatement> callableStatements = new ArrayList<>();

        private PoolConnection(Connection connection) {
            this.connection = connection;
        }

        private void closeStatements() {
            for (Statement st : statements) {
                try {
                    if (st != null && !st.isClosed()) {
                        st.close();
                        statements.remove(st);
                    }
                } catch (SQLException e) {
                    logger.log(Level.WARN, "Cannot close statement", e);
                }
                statements = statements.stream().filter(Objects::nonNull).collect(Collectors.toList());
            }
            for (PreparedStatement ps : preparedStatements) {
                try {
                    if (ps != null && !ps.isClosed()) {
                        ps.close();
                        statements.remove(ps);
                    }
                } catch (SQLException e) {
                    logger.log(Level.WARN, "Cannot close prepared statement", e);
                }
                preparedStatements = preparedStatements.stream().filter(Objects::nonNull).collect(Collectors.toList());
            }
            for (CallableStatement cs : callableStatements) {
                try {
                    if (cs != null && !cs.isClosed()) {
                        cs.close();
                        statements.remove(cs);
                    }
                } catch (SQLException e) {
                    logger.log(Level.WARN, "Cannot close callable statement", e);
                }
                callableStatements = callableStatements.stream().filter(Objects::nonNull).collect(Collectors.toList());
            }
        }

        private void shutdown() throws SQLException {
            connection.close();
        }

        public long getLastUsageTimeStamp() {
            return lastUsageTimeStamp;
        }

        public Connection getConnection() {
            return connection;
        }

        @Override
        public void close() {
            lastUsageTimeStamp = System.currentTimeMillis();
            releaseConnection(this);
        }

        @Override
        public void beginRequest() throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
            connection.beginRequest();
        }

        @Override
        public void endRequest() throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
            connection.endRequest();
        }

        @Override
        public boolean setShardingKeyIfValid(ShardingKey shardingKey, ShardingKey superShardingKey, int timeout) throws
                SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
            return connection.setShardingKeyIfValid(shardingKey, superShardingKey, timeout);
        }

        @Override
        public boolean setShardingKeyIfValid(ShardingKey shardingKey, int timeout) throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
            return connection.setShardingKeyIfValid(shardingKey, timeout);
        }

        @Override
        public void setShardingKey(ShardingKey shardingKey, ShardingKey superShardingKey) throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
            connection.setShardingKey(shardingKey, superShardingKey);
        }

        @Override
        public void setShardingKey(ShardingKey shardingKey) throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
            connection.setShardingKey(shardingKey);
        }

        @Override
        public Statement createStatement() throws SQLException {
            Statement st = connection.createStatement();
            statements.add(st);
            lastUsageTimeStamp = System.currentTimeMillis();
            return st;
        }

        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException {
            PreparedStatement ps = connection.prepareStatement(sql);
            preparedStatements.add(ps);
            lastUsageTimeStamp = System.currentTimeMillis();
            return ps;
        }

        @Override
        public CallableStatement prepareCall(String sql) throws SQLException {
            CallableStatement cs = connection.prepareCall(sql);
            callableStatements.add(cs);
            lastUsageTimeStamp = System.currentTimeMillis();
            return cs;
        }

        @Override
        public String nativeSQL(String sql) throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
            return connection.nativeSQL(sql);
        }

        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
            connection.setAutoCommit(autoCommit);
        }

        @Override
        public boolean getAutoCommit() throws SQLException {
            return connection.getAutoCommit();
        }

        @Override
        public void commit() throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
            connection.commit();
        }

        @Override
        public void rollback() throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
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
            lastUsageTimeStamp = System.currentTimeMillis();
            connection.setReadOnly(readOnly);
        }

        @Override
        public boolean isReadOnly() throws SQLException {
            return connection.isReadOnly();
        }

        @Override
        public void setCatalog(String catalog) throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
            connection.setCatalog(catalog);
        }

        @Override
        public String getCatalog() throws SQLException {
            return connection.getCatalog();
        }

        @Override
        public void setTransactionIsolation(int level) throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
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
            lastUsageTimeStamp = System.currentTimeMillis();
            connection.clearWarnings();
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            Statement st = connection.createStatement(resultSetType, resultSetConcurrency);
            statements.add(st);
            lastUsageTimeStamp = System.currentTimeMillis();
            return st;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
                throws SQLException {
            PreparedStatement ps = connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
            preparedStatements.add(ps);
            lastUsageTimeStamp = System.currentTimeMillis();
            return ps;
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
                throws SQLException {
            CallableStatement cs = connection.prepareCall(sql, resultSetType, resultSetConcurrency);
            callableStatements.add(cs);
            lastUsageTimeStamp = System.currentTimeMillis();
            return cs;
        }

        @Override
        public Map<String, Class<?>> getTypeMap() throws SQLException {
            return connection.getTypeMap();
        }

        @Override
        public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
            connection.setTypeMap(map);
        }

        @Override
        public void setHoldability(int holdability) throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
            connection.setHoldability(holdability);
        }

        @Override
        public int getHoldability() throws SQLException {
            return connection.getHoldability();
        }

        @Override
        public Savepoint setSavepoint() throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
            return connection.setSavepoint();
        }

        @Override
        public Savepoint setSavepoint(String name) throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
            return connection.setSavepoint(name);
        }

        @Override
        public void rollback(Savepoint savepoint) throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
            connection.rollback(savepoint);
        }

        @Override
        public void releaseSavepoint(Savepoint savepoint) throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
            connection.releaseSavepoint(savepoint);
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
                throws SQLException {
            Statement st = connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
            statements.add(st);
            lastUsageTimeStamp = System.currentTimeMillis();
            return st;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
                                                  int resultSetHoldability) throws SQLException {
            PreparedStatement ps = connection.prepareStatement(sql, resultSetType, resultSetConcurrency,
                    resultSetHoldability);
            preparedStatements.add(ps);
            lastUsageTimeStamp = System.currentTimeMillis();
            return ps;
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
                                             int resultSetHoldability) throws SQLException {
            CallableStatement cs = connection.prepareCall(sql, resultSetType, resultSetConcurrency,
                    resultSetHoldability);
            callableStatements.add(cs);
            lastUsageTimeStamp = System.currentTimeMillis();
            return cs;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            PreparedStatement ps = connection.prepareStatement(sql, autoGeneratedKeys);
            preparedStatements.add(ps);
            lastUsageTimeStamp = System.currentTimeMillis();
            return ps;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
            PreparedStatement ps = connection.prepareStatement(sql, columnIndexes);
            preparedStatements.add(ps);
            lastUsageTimeStamp = System.currentTimeMillis();
            return ps;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
            PreparedStatement ps = connection.prepareStatement(sql, columnNames);
            preparedStatements.add(ps);
            lastUsageTimeStamp = System.currentTimeMillis();
            return ps;
        }

        @Override
        public Clob createClob() throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
            return connection.createClob();
        }

        @Override
        public Blob createBlob() throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
            return connection.createBlob();
        }

        @Override
        public NClob createNClob() throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
            return connection.createNClob();
        }

        @Override
        public SQLXML createSQLXML() throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
            return connection.createSQLXML();
        }

        @Override
        public boolean isValid(int timeout) throws SQLException {
            return connection.isValid(timeout);
        }

        @Override
        public void setClientInfo(String name, String value) throws SQLClientInfoException {
            lastUsageTimeStamp = System.currentTimeMillis();
            connection.setClientInfo(name, value);
        }

        @Override
        public void setClientInfo(Properties properties) throws SQLClientInfoException {
            lastUsageTimeStamp = System.currentTimeMillis();
            connection.setClientInfo(properties);
        }

        @Override
        public String getClientInfo(String name) throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
            return connection.getClientInfo(name);
        }

        @Override
        public Properties getClientInfo() throws SQLException {
            return connection.getClientInfo();
        }

        @Override
        public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
            return connection.createArrayOf(typeName, elements);
        }

        @Override
        public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
            return connection.createStruct(typeName, attributes);
        }

        @Override
        public void setSchema(String schema) throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
            connection.setSchema(schema);
        }

        @Override
        public String getSchema() throws SQLException {
            return connection.getSchema();
        }

        @Override
        public void abort(Executor executor) throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
            connection.abort(executor);
        }

        @Override
        public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
            connection.setNetworkTimeout(executor, milliseconds);
        }

        @Override
        public int getNetworkTimeout() throws SQLException {
            return connection.getNetworkTimeout();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            lastUsageTimeStamp = System.currentTimeMillis();
            return connection.unwrap(iface);
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return connection.isWrapperFor(iface);
        }

        @Override
        public int hashCode() {
            return connection.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || PoolConnection.class != obj.getClass()) {
                return false;
            } else {
                PoolConnection other = (PoolConnection) obj;
                return this.connection.equals(other.getConnection());
            }
        }

        @Override
        public String toString() {
            return connection.toString();
        }
    }

}