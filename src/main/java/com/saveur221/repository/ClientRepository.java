package com.saveur221.repository;

import com.saveur221.config.DatabaseConfig;
import com.saveur221.entities.Client;
import com.saveur221.entities.Role;

import java.sql.*;
import java.util.Optional;

/**
 * Lecture des clients (table clients, jointe à utilisateurs — héritage
 * Client -> Utilisateur du diagramme de classes). Le Module A ne crée pas
 * de comptes clients (rôle du module PHP) ; il les consulte seulement.
 */
public class ClientRepository {

    private static final String SELECT_BASE =
        "SELECT u.id, u.nom, u.prenom, u.email, u.mdp, u.actif, u.date_creation, " +
        "       r.id AS role_id, r.libelle AS role_libelle, " +
        "       cl.telephone, cl.adresse " +
        "FROM clients cl " +
        "JOIN utilisateurs u ON u.id = cl.id " +
        "JOIN roles r ON r.id = u.role_id ";

    public Optional<Client> trouverParId(int id) {
        String sql = SELECT_BASE + "WHERE u.id = ?";
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapper(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du client #" + id, e);
        }
        return Optional.empty();
    }

    private Client mapper(ResultSet rs) throws SQLException {
        Role role = new Role(rs.getInt("role_id"), rs.getString("role_libelle"));
        Timestamp ts = rs.getTimestamp("date_creation");
        return new Client(
                rs.getInt("id"), rs.getString("nom"), rs.getString("prenom"), rs.getString("email"),
                rs.getString("mdp"), rs.getBoolean("actif"), ts != null ? ts.toLocalDateTime() : null,
                role, rs.getString("telephone"), rs.getString("adresse"));
    }
}