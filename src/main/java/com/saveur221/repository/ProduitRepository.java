package com.saveur221.repository;

import com.saveur221.config.DatabaseConfig;
import com.saveur221.entities.Categorie;
import com.saveur221.entities.Produit;
import com.saveur221.enums.StatutProduit;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProduitRepository {

    private static final String SELECT_BASE =
        "SELECT p.id, p.nom, p.description, p.prix, p.stock, p.image, p.statut, " +
        "       c.id AS categorie_id, c.libelle AS categorie_libelle, c.description AS categorie_description " +
        "FROM produits p JOIN categories c ON c.id = p.categorie_id ";

    public List<Produit> lister() {
        return executerListe(SELECT_BASE + "ORDER BY p.nom", ps -> {});
    }

    public Optional<Produit> trouverParId(int id) {
        String sql = SELECT_BASE + "WHERE p.id = ?";
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapper(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du produit #" + id, e);
        }
        return Optional.empty();
    }

    public List<Produit> rechercherParLibelle(String motCle) {
        return executerListe(SELECT_BASE + "WHERE LOWER(p.nom) LIKE ? ORDER BY p.nom",
                ps -> ps.setString(1, "%" + motCle.toLowerCase() + "%"));
    }

    public List<Produit> filtrerParCategorie(int categorieId) {
        return executerListe(SELECT_BASE + "WHERE c.id = ? ORDER BY p.nom",
                ps -> ps.setInt(1, categorieId));
    }

    public List<Produit> filtrerParDisponibilite(boolean disponible) {
        String statut = disponible ? "disponible" : "en_rupture";
        return executerListe(SELECT_BASE + "WHERE p.statut = ?::statut_produit_enum ORDER BY p.nom",
                ps -> ps.setString(1, statut));
    }

    public boolean existeParNom(String nom) {
        String sql = "SELECT 1 FROM produits WHERE LOWER(nom) = LOWER(?)";
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, nom);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification d'unicité du produit", e);
        }
    }

    public Produit creer(Produit p) {
        String sql = "INSERT INTO produits (nom, description, prix, stock, image, statut, categorie_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?::statut_produit_enum, ?)";
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getDescription());
            ps.setBigDecimal(3, p.getPrix());
            ps.setInt(4, p.getStock());
            ps.setString(5, p.getImage());
            ps.setString(6, p.getStatut().getValeurBdd());
            ps.setInt(7, p.getCategorie().getId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.setId(keys.getInt(1));
            }
            return p;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création du produit", e);
        }
    }

    public void mettreAJour(Produit p) {
        String sql = "UPDATE produits SET nom=?, description=?, prix=?, image=?, statut=?::statut_produit_enum, categorie_id=? " +
                     "WHERE id=?";
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getDescription());
            ps.setBigDecimal(3, p.getPrix());
            ps.setString(4, p.getImage());
            ps.setString(5, p.getStatut().getValeurBdd());
            ps.setInt(6, p.getCategorie().getId());
            ps.setInt(7, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du produit #" + p.getId(), e);
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM produits WHERE id = ?";
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du produit #" + id, e);
        }
    }

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<Produit> executerListe(String sql, Binder binder) {
        List<Produit> resultat = new ArrayList<>();
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) resultat.add(mapper(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des produits", e);
        }
        return resultat;
    }

    private Produit mapper(ResultSet rs) throws SQLException {
        Categorie categorie = new Categorie(rs.getInt("categorie_id"),
                rs.getString("categorie_libelle"), rs.getString("categorie_description"));
        return new Produit(rs.getInt("id"), rs.getString("nom"), rs.getString("description"),
                rs.getBigDecimal("prix"), rs.getInt("stock"), rs.getString("image"),
                StatutProduit.fromValeurBdd(rs.getString("statut")), categorie);
    }
}