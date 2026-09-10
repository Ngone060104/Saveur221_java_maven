package com.saveur221.repository;

import com.saveur221.config.DatabaseConfig;
import com.saveur221.entities.Role;
import com.saveur221.entities.Utilisateur;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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
                if (rs.next()) return Optional.of(mapper(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'utilisateur par email", e);
        }
        return Optional.empty();
    }

    public Optional<Utilisateur> trouverParId(int id) {
        String sql = SELECT_BASE + "WHERE u.id = ?";
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapper(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'utilisateur #" + id, e);
        }
        return Optional.empty();
    }

    public boolean existeParEmail(String email) {
        String sql = "SELECT 1 FROM utilisateurs WHERE email = ?";
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification d'unicité de l'email", e);
        }
    }

    /** Personnel interne uniquement (ADMIN, GERANT) — les clients ne sont pas gérés ici. */
    public List<Utilisateur> listerPersonnelInterne() {
        String sql = SELECT_BASE + "WHERE r.libelle IN ('ADMIN','GERANT') ORDER BY u.nom, u.prenom";
        List<Utilisateur> resultat = new ArrayList<>();
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) resultat.add(mapper(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du listage des utilisateurs internes", e);
        }
        return resultat;
    }

    public List<Utilisateur> rechercherParNomInterne(String motCle) {
        String sql = SELECT_BASE +
            "WHERE r.libelle IN ('ADMIN','GERANT') " +
            "AND (LOWER(u.nom) LIKE ? OR LOWER(u.prenom) LIKE ? OR LOWER(u.email) LIKE ?) " +
            "ORDER BY u.nom, u.prenom";
        String like = "%" + motCle.toLowerCase() + "%";
        List<Utilisateur> resultat = new ArrayList<>();
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) resultat.add(mapper(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche d'utilisateurs", e);
        }
        return resultat;
    }

    public Utilisateur creer(Utilisateur u) {
        String sql = "INSERT INTO utilisateurs (nom, prenom, email, mdp, actif, role_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getMdp());
            ps.setBoolean(5, u.isActif());
            ps.setInt(6, u.getRole().getId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) u.setId(keys.getInt(1));
            }
            return u;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création de l'utilisateur", e);
        }
    }

    public void mettreAJour(Utilisateur u) {
        String sql = "UPDATE utilisateurs SET nom=?, prenom=?, email=?, role_id=? WHERE id=?";
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setInt(4, u.getRole().getId());
            ps.setInt(5, u.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'utilisateur #" + u.getId(), e);
        }
    }

    public void changerActif(int id, boolean actif) {
        String sql = "UPDATE utilisateurs SET actif=? WHERE id=?";
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setBoolean(1, actif);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du changement de statut actif", e);
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM utilisateurs WHERE id=?";
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'utilisateur #" + id, e);
        }
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