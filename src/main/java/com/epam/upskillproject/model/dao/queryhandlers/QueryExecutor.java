package com.epam.upskillproject.model.dao.queryhandlers;

import com.epam.upskillproject.model.dto.StatusType;
import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;

@Stateless
public class QueryExecutor {

    private static final Logger logger = LogManager.getLogger(QueryExecutor.class.getName());

    @Resource(lookup = "java:global/customProjectDB")
    private DataSource dataSource;

    /**
     * Creates a statement and executes passed query
     * @param query a sql-query without placeholders (ready to execute)
     * @return ResultSet
     * @throws SQLException
     */
    public ResultSet execute(String query) throws SQLException {
        if (checkParams(query)) {
            try (Connection conn = dataSource.getConnection()) {
                try (Statement statement = conn.createStatement()) {
                    return statement.executeQuery(query);
                }
            }
        } else {
            return null;
        }
    }

    /**
     * Creates a prepared statement and executes passed query with substitution of passed parameters
     * @param query a raw sql-query with placeholders (ready to prepare)
     * @param params params to preparing a sql statement
     * @return ResultSet
     * @throws SQLException
     */
    public ResultSet execute(String query, Object... params) throws SQLException {
        if (checkParams(query) && checkParams(params)) {
            try (Connection conn = dataSource.getConnection()) {
                try (PreparedStatement statement = conn.prepareStatement(query)) {
                    putParams(statement, params);
                    return statement.executeQuery();
                }
            }
        } else {
            return null;
        }
    }

    /**
     * Creates a prepared statement based on passed query with substitution of passed parameters and then calls
     * executeUpdate()
     * @param query a raw sql-query with placeholders (ready to prepare)
     * @param params params to preparing a sql statement
     * @return the row count for SQL statements or 0 for SQL statements that return nothing
     * @throws SQLException
     */
    public int executeUpdate(String query, Object... params) throws SQLException {
        if (checkParams(query) && checkParams(params)) {
            try (Connection conn = dataSource.getConnection()) {
                try (PreparedStatement statement = conn.prepareStatement(query)) {
                    putParams(statement, params);
                    return statement.executeUpdate();
                }
            }
        } else {
            return 0;
        }
    }

    /**
     * Creates a prepared statement based on passed query with substitution of passed parameters and then calls
     * executeUpdate(). Uses а previously created connection to implement a transaction
     * @param query a raw sql-query with placeholders (ready to prepare)
     * @param params params to preparing a sql statement
     * @return the row count for SQL statements or 0 for SQL statements that return nothing
     * @throws SQLException
     */
    public int executeUpdate(Connection conn, String query, Object... params) throws SQLException {
        if (checkParams(conn, query) && checkParams(params)) {
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                putParams(statement, params);
                return statement.executeUpdate();
            }
        } else {
            return 0;
        }
    }

    private void putParams(PreparedStatement statement, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            if (params[i] instanceof String) {
                statement.setString(i + 1, (String) params[i]);
            } else if (params[i] instanceof Integer) {
                statement.setInt(i + 1, (Integer) params[i]);
            } else if (params[i] instanceof BigInteger) {
                statement.setString(i + 1, params[i].toString());
            } else if (params[i] instanceof BigDecimal) {
                statement.setBigDecimal(i + 1, (BigDecimal) params[i]);
            } else if (params[i] instanceof StatusType) {
                statement.setInt(i + 1, ((StatusType) params[i]).getId());
            } else if (params[i] instanceof Timestamp) {
                statement.setTimestamp(i + 1, (Timestamp) params[i]);
            } else if (params[i] instanceof Date) {
                statement.setDate(i + 1, (Date) params[i]);
            }  else {
                logger.log(Level.WARN, "Illegal type of passed parameter: " + params[i].getClass());
                throw new SQLException("Illegal SQL parameters passed");
            }
        }
    }

    private boolean checkParams(Object... params) throws SQLException {
        if (params == null) {
            logger.log(Level.INFO, String.format("Parameters array passed to %s is null",
                    Thread.currentThread().getStackTrace()[1].getMethodName()));
            return false;
        }
        for (Object p : params) {
            if (
                    p == null ||
                            (p instanceof Connection && !((Connection) p).isValid(5)) ||
                            (p instanceof String && ((String) p).trim().length() == 0) ||
                            (p instanceof Integer && ((Integer) p) < 0) ||
                            (p instanceof BigDecimal && ((BigDecimal) p).compareTo(BigDecimal.ZERO) < 0) ||
                            (p instanceof BigInteger && ((BigInteger) p).compareTo(BigInteger.ZERO) < 0)
            ) {
                logger.log(Level.INFO, String.format("At least one of the passed parameters to %s is not valid",
                        Thread.currentThread().getStackTrace()[1].getMethodName()));
                return false;
            }
        }
        return true;
    }

}
