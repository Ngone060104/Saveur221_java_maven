package com.saveur221.repository;

import com.saveur221.config.DatabaseConfig;
import com.saveur221.entities.Role;
import com.saveur221.entities.Utilisateur;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

public class UtilisateurRepository {

    private static final String SELECT_BASE =
        "SELECT u.id, u.nom, u.prenom, u.email, u.mdp, u.actif, u.date_creation, " +
        "       r.id AS role_id, r.libelle AS role_libelle " +
        "FROM utilisateurs u JOIN roles r ON r.id = u.role_id ";

    public Optional<Utilisateur> trouverParEmail(String email) {
        String sql = SELECT_BASE + "WHERE u.email = ?";
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapper(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'utilisateur par email", e);
        }
        return Optional.empty();
    }

    private Utilisateur mapper(ResultSet rs) throws SQLException {
        Role role = new Role(rs.getInt("role_id"), rs.getString("role_libelle"));
        Timestamp ts = rs.getTimestamp("date_creation");
        return new Utilisateur(
                rs.getInt("id"), rs.getString("nom"), rs.getString("prenom"),
                rs.getString("email"), rs.getString("mdp"), rs.getBoolean("actif"),
                ts != null ? ts.toLocalDateTime() : null, role);
    }
}