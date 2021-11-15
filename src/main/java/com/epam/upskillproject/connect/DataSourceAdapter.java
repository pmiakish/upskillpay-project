package com.epam.upskillproject.connect;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import org.apache.commons.dbcp2.BasicDataSource;

@Singleton(name = "projectDB")
public class DataSourceAdapter extends BasicDataSource {

    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.hsqldb.jdbc.JDBCDriver";
    private static final String DB_URL = "jdbc:hsqldb:mem:projectDb";

    //  Database credentials
    private static final String USER = "root";
    private static final String PASS = "root";

    private static final int MIN_POOL_SIZE = 5;
    private static final int ACQUIRE_INCREMENT = 5;
    private static final int MAX_POOL_SIZE = 20;
    private static final int TIMEOUT = 30;

    @PostConstruct
    private void configureBean() {
        this.setDriverClassName(JDBC_DRIVER);
        this.setUrl(DB_URL);
        this.setUsername(USER);
        this.setPassword(PASS);
        this.setInitialSize(MIN_POOL_SIZE);
        this.setMinIdle(MIN_POOL_SIZE);
        this.setMaxTotal(MAX_POOL_SIZE);
        this.setRemoveAbandonedTimeout(TIMEOUT);
    }

}
