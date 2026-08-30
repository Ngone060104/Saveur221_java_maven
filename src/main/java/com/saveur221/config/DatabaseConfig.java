package com.saveur221.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Fournit une connexion JDBC à la base PostgreSQL "saveur221",
 * partagée avec le module PHP (mêmes tables, mêmes règles métier).
 *
 * Les paramètres se règlent dans src/main/resources/config.properties
 * (voir config.properties.example) ou via variables d'environnement,
 * pratique pour ne jamais committer d'identifiants réels.
 */
public final class DatabaseConfig {

    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/saveur221";
    private static final String DEFAULT_USER = "postgres";
    private static final String DEFAULT_PASSWORD = "postgres";

    private static final Properties properties = loadProperties();

    private DatabaseConfig() {
        // classe utilitaire, non instanciable
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = DatabaseConfig.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            System.err.println("Impossible de lire config.properties, utilisation des valeurs par défaut : " + e.getMessage());
        }
        return props;
    }

    private static String get(String key, String envKey, String defaultValue) {
        String fromEnv = System.getenv(envKey);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return properties.getProperty(key, defaultValue);
    }

    public static String getUrl() {
        return get("db.url", "SAVEUR221_DB_URL", DEFAULT_URL);
    }

    public static String getUser() {
        return get("db.user", "SAVEUR221_DB_USER", DEFAULT_USER);
    }

    public static String getPassword() {
        return get("db.password", "SAVEUR221_DB_PASSWORD", DEFAULT_PASSWORD);
    }

    /**
     * Ouvre une nouvelle connexion JDBC. Chaque appelant est responsable
     * de la fermer (try-with-resources) pour éviter les fuites de connexion.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getUrl(), getUser(), getPassword());
    }
}
