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

    private static final int VALIDATION_TIMEOUT_VALUE_SEC = 3;

    @Resource(lookup = "java:global/customProjectDB")
    private DataSource dataSource;

    /**
     * Creates a statement and executes passed query.
     * Notice that the method will not close a passed connection independently
     * @param conn a valid java.sql.Connection
     * @param query a String representing sql-query (ready to execute)
     * @return ResultSet
     * @throws SQLException
     */
    public ResultSet execute(Connection conn, String query) throws SQLException {
        if (checkConnection(conn) && checkParams(query)) {
            Statement statement = conn.createStatement();
            return statement.executeQuery(query);
        } else {
            return null;
        }
    }

    /**
     * Creates a prepared statement and executes passed query with substitution of passed parameters.
     * Notice that the method will not close an obtained statement and a passed connection independently
     * @param conn a valid java.sql.Connection
     * @param rawQuery a String representing raw sql-query with placeholders (ready to prepare)
     * @param params parameters for preparing a sql-statement
     * @return ResultSet
     * @throws SQLException
     */
    public ResultSet execute(Connection conn, String rawQuery, Object... params) throws SQLException {
        if (checkConnection(conn) && checkParams(rawQuery) && checkParams(params)) {
            PreparedStatement statement = conn.prepareStatement(rawQuery);
            putParams(statement, params);
            return statement.executeQuery();
        } else {
            return null;
        }
    }

    /**
     * Creates a prepared statement based on passed query with substitution of passed parameters and then calls
     * executeUpdate()
     * Notice that the method will close an obtained statement after execution but a passed connection still will not be
     * closed
     * @param conn a valid java.sql.Connection
     * @param rawQuery a String representing raw sql-query with placeholders (ready to prepare)
     * @param params parameters for preparing a sql-statement
     * @return ResultSet
     * @throws SQLException
     */
    public synchronized int executeUpdate(Connection conn, String rawQuery, Object... params) throws SQLException {
        int result = 0;
        if (checkConnection(conn) && checkParams(rawQuery) && checkParams(params)) {
            PreparedStatement statement = conn.prepareStatement(rawQuery);
            putParams(statement, params);
            result = statement.executeUpdate();
            statement.close();
        }
        return result;
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
                throw new SQLException("Illegal SQL parameter passed");
            }
        }
    }
    // TODO decomposition?
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

    private boolean checkConnection(Connection conn) throws SQLException {
        boolean isValid = (conn != null && conn.isValid(VALIDATION_TIMEOUT_VALUE_SEC));
        if (!isValid) {
            logger.log(Level.INFO, "Passed connection is not valid");
        }
        return isValid;
    }

}
