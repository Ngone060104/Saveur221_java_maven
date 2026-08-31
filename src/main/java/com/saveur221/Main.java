package com.saveur221;

import com.saveur221.config.DatabaseConfig;
import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        try (Connection cnx = DatabaseConfig.getConnection()) {
            System.out.println("✓ Connexion réussie à : " + cnx.getCatalog());
        } catch (Exception e) {
            System.out.println("✗ Échec de connexion : " + e.getMessage());
        }
    }
}