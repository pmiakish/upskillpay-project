package com.epam.upskillproject.init;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Singleton
public class PropertiesKeeper {

    private static final Logger logger = LogManager.getLogger(PropertiesKeeper.class.getName());

    private static final String[] resources = {
            "application.properties",
            "db.properties",
            "html.properties",
            "sql.properties",
            "passwordhash.properties"
    };
    private Properties properties = new Properties();

    @PostConstruct
    private void collectProperties() {
        Properties collectedProperties = new Properties();
        Arrays.stream(resources).forEach(resource -> collectedProperties.putAll(readPropertiesFromFile(resource)));
        this.properties = collectedProperties;
    }

    private Properties readPropertiesFromFile(String resourceName) {
        Properties readProperties = new Properties();
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            Path path = Path.of(Objects.requireNonNull(classLoader.getResource(resourceName)).toURI());
            readProperties.load(new FileInputStream(path.toString()));
        } catch (IOException | URISyntaxException e) {
            logger.log(Level.ERROR, "Cannot read properties from resource: " + resourceName, e);
        }
        return readProperties;
    }

    public Properties getProperties() {
        collectProperties();
        return properties;
    }

    public String getString(String key) throws IllegalArgumentException {
        if (key == null) {
            logger.log(Level.WARN, "Passed property key is null");
            throw new IllegalArgumentException("Property key may not be null");
        }
        collectProperties();
        String propValue = properties.getProperty(key);
        if (propValue != null) {
            return propValue;
        } else {
            logger.log(Level.WARN, "Property not found, key: " + key);
            throw new IllegalArgumentException("Property '" + key + "' not found");
        }
    }

    public String getStringOrDefault(String key, String defaultValue) throws IllegalArgumentException {
        if (key == null || defaultValue == null) {
            logger.log(Level.WARN, "Passed property key or default value is null");
            throw new IllegalArgumentException("Property key and default value may not be null");
        }
        collectProperties();
        String propValue = properties.getProperty(key);
        return (propValue != null) ? propValue : defaultValue;
    }

    public int getInt(String key) throws IllegalArgumentException {
        if (key == null) {
            logger.log(Level.WARN, "Passed property key is null");
            throw new IllegalArgumentException("Property key may not be null");
        }
        collectProperties();
        String propStrValue = properties.getProperty(key);
        if (propStrValue != null) {
            try {
                return Integer.parseInt(propStrValue);
            } catch (NumberFormatException e) {
                logger.log(Level.WARN, "Property value is not integer, key: " + key, e);
                throw new IllegalArgumentException("Value of '" + key + "' is not integer");
            }
        } else {
            logger.log(Level.WARN, "Property not found, key: " + key);
            throw new IllegalArgumentException("Property '" + key + "' not found");
        }
    }

    public int getIntOrDefault(String key, int defaultValue) throws IllegalArgumentException {
        if (key == null) {
            logger.log(Level.WARN, "Passed property key is null");
            throw new IllegalArgumentException("Property key may not be null");
        }
        collectProperties();
        String propStrValue = properties.getProperty(key);
        try {
            return Integer.parseInt(propStrValue);
        } catch (NullPointerException | NumberFormatException e) {
            return defaultValue;
        }
    }

    public BigDecimal getBigDecimal(String key) throws IllegalArgumentException {
        if (key == null) {
            logger.log(Level.WARN, "Passed property key is null");
            throw new IllegalArgumentException("Property key may not be null");
        }
        collectProperties();
        String propStrValue = properties.getProperty(key);
        if (propStrValue != null) {
            try {
                return new BigDecimal(propStrValue);
            } catch (NumberFormatException e) {
                logger.log(Level.WARN, "Property value is not decimal, key: " + key, e);
                throw new IllegalArgumentException("Value of '" + key + "' is not BigDecimal");
            }
        } else {
            logger.log(Level.WARN, "Property not found, key: " + key);
            throw new IllegalArgumentException("Property '" + key + "' not found");
        }
    }

    public BigDecimal getBigDecimalOrDefault(String key, BigDecimal defaultValue) throws IllegalArgumentException {
        if (key == null || defaultValue == null) {
            logger.log(Level.WARN, "Passed property key or default value is null");
            throw new IllegalArgumentException("Property key and default value may not be null");
        }
        collectProperties();
        String propStrValue = properties.getProperty(key);
        try {
            return new BigDecimal(propStrValue);
        } catch (NullPointerException | NumberFormatException e) {
            return defaultValue;
        }
    }

    public TimeUnit getTimeUnit(String key) throws IllegalArgumentException {
        if (key == null) {
            logger.log(Level.WARN, "Passed property key is null");
            throw new IllegalArgumentException("Property key may not be null");
        }
        collectProperties();
        String propStrValue = properties.getProperty(key);
        if (propStrValue != null) {
            try {
                return TimeUnit.valueOf(propStrValue);
            } catch (IllegalArgumentException e) {
                logger.log(Level.WARN, "Property value is not timeunit, key: " + key, e);
                throw new IllegalArgumentException("Value of '" + key + "' is not a constant of TimeUnit");
            }
        } else {
            logger.log(Level.WARN, "Property not found, key: " + key);
            throw new IllegalArgumentException("Property '" + key + "' not found");
        }
    }

    public TimeUnit getTimeUnitOrDefault(String key, TimeUnit defaultValue) throws IllegalArgumentException {
        if (key == null || defaultValue == null) {
            logger.log(Level.WARN, "Passed property key or default value is null");
            throw new IllegalArgumentException("Property key and default value cannot be null");
        }
        collectProperties();
        String propStrValue = properties.getProperty(key);
        try {
            return TimeUnit.valueOf(propStrValue);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    public static String[] getResources() {
        return resources;
    }

}
