package com.saveur221.repository;

import com.saveur221.config.DatabaseConfig;
import com.saveur221.entities.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class RoleRepository {

    public Optional<Role> trouverParId(int id) {
        String sql = "SELECT id, libelle FROM roles WHERE id = ?";
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Role(rs.getInt("id"), rs.getString("libelle")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du rôle #" + id, e);
        }
        return Optional.empty();
    }
}