package com.epam.upskillproject.util.init;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.sql.DataSource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

@Singleton
@Startup
public class DataBaseInitializer {

    private static final Logger logger = LogManager.getLogger(DataBaseInitializer.class.getName());

    private static final String SCHEMA_PATH = "sql/schema.sql";
    private static final String DATA_PATH = "sql/data.sql";

    @Resource(lookup = "java:global/customProjectDB")
    private DataSource dataSource;

    @PostConstruct
    public void init() {
        try (Connection conn = dataSource.getConnection()) {

            try (Statement statement = conn.createStatement()) {
                statement.execute(getSql(SCHEMA_PATH));
            }
            try (Statement statement = conn.createStatement()) {
                statement.execute(getSql(DATA_PATH));
            }
        } catch (SQLException e) {
            logger.log(Level.FATAL, "Cannot initialize database", e);
        }
    }

    private String getSql(final String resourceName) {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            Path path = Path.of(Objects.requireNonNull(classLoader.getResource(resourceName)).toURI());
            return String.join("\n", Files.readAllLines(path));
        } catch (IOException | URISyntaxException e) {
            logger.log(Level.FATAL, "Cannot read SQL-resource for database initialization", e);
            return "";
        }
    }
}
