package com.saveur221.repository;

import com.saveur221.config.DatabaseConfig;
import com.saveur221.entities.Categorie;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategorieRepository {

    public List<Categorie> lister() {
        String sql = "SELECT id, libelle, description FROM categories ORDER BY libelle";
        List<Categorie> resultat = new ArrayList<>();
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) resultat.add(mapper(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du listage des catégories", e);
        }
        return resultat;
    }

    public Optional<Categorie> trouverParId(int id) {
        String sql = "SELECT id, libelle, description FROM categories WHERE id = ?";
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapper(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de la catégorie #" + id, e);
        }
        return Optional.empty();
    }

    public List<Categorie> rechercherParNom(String motCle) {
        String sql = "SELECT id, libelle, description FROM categories WHERE LOWER(libelle) LIKE ? ORDER BY libelle";
        List<Categorie> resultat = new ArrayList<>();
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, "%" + motCle.toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) resultat.add(mapper(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de catégories", e);
        }
        return resultat;
    }

    public boolean existeParLibelle(String libelle) {
        String sql = "SELECT 1 FROM categories WHERE LOWER(libelle) = LOWER(?)";
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, libelle);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification d'unicité", e);
        }
    }

    /** Utilisé pour bloquer la suppression si des produits sont liés. */
    public int compterProduitsLies(int categorieId) {
        String sql = "SELECT COUNT(*) FROM produits WHERE categorie_id = ?";
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, categorieId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des produits liés", e);
        }
    }

    public Categorie creer(Categorie c) {
        String sql = "INSERT INTO categories (libelle, description) VALUES (?, ?)";
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getLibelle());
            ps.setString(2, c.getDescription());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) c.setId(keys.getInt(1));
            }
            return c;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création de la catégorie", e);
        }
    }

    public void mettreAJour(Categorie c) {
        String sql = "UPDATE categories SET libelle = ?, description = ? WHERE id = ?";
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, c.getLibelle());
            ps.setString(2, c.getDescription());
            ps.setInt(3, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de la catégorie #" + c.getId(), e);
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de la catégorie #" + id, e);
        }
    }

    private Categorie mapper(ResultSet rs) throws SQLException {
        return new Categorie(rs.getInt("id"), rs.getString("libelle"), rs.getString("description"));
    }
}