package com.saveur221.repository;

import com.saveur221.config.DatabaseConfig;
import com.saveur221.entities.Categorie;
import com.saveur221.entities.LigneCommande;
import com.saveur221.entities.Produit;
import com.saveur221.enums.StatutProduit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LigneCommandeRepository {

    private static final String SELECT_BASE =
        "SELECT lc.id, lc.quantite, lc.prix_unitaire, lc.montant_ligne, lc.commande_id, " +
        "       p.id AS produit_id, p.nom AS produit_nom, p.description AS produit_description, " +
        "       p.prix AS produit_prix, p.stock AS produit_stock, p.image AS produit_image, p.statut AS produit_statut, " +
        "       c.id AS categorie_id, c.nom AS categorie_nom, c.description AS categorie_description " +
        "FROM lignes_commande lc " +
        "JOIN produits p ON p.id = lc.produit_id " +
        "JOIN categories c ON c.id = p.categorie_id ";

    public List<LigneCommande> listerParCommande(int commandeId) {
        String sql = SELECT_BASE + "WHERE lc.commande_id = ? ORDER BY lc.id";
        List<LigneCommande> resultat = new ArrayList<>();
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, commandeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) resultat.add(mapper(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des lignes de la commande #" + commandeId, e);
        }
        return resultat;
    }

    /**
     * Produits les plus vendus (par quantité cumulée), commandes annulées exclues.
     * @param limite nombre maximum de résultats (0 = pas de limite)
     */
    public List<ProduitVendu> produitsLesPlusVendus(int limite) {
        String sql =
            "SELECT p.id, p.nom, SUM(lc.quantite) AS total_vendu " +
            "FROM lignes_commande lc " +
            "JOIN produits p ON p.id = lc.produit_id " +
            "JOIN commandes c ON c.id = lc.commande_id " +
            "WHERE c.statut <> 'ANNULEE' " +
            "GROUP BY p.id, p.nom " +
            "ORDER BY total_vendu DESC" +
            (limite > 0 ? " LIMIT " + limite : "");
        List<ProduitVendu> resultat = new ArrayList<>();
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultat.add(new ProduitVendu(
                        rs.getInt("id"), rs.getString("nom"), rs.getLong("total_vendu")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du calcul des produits les plus vendus", e);
        }
        return resultat;
    }

    private LigneCommande mapper(ResultSet rs) throws SQLException {
        Categorie categorie = new Categorie(
                rs.getInt("categorie_id"), rs.getString("categorie_nom"), rs.getString("categorie_description"));
        Produit produit = new Produit(
                rs.getInt("produit_id"), rs.getString("produit_nom"), rs.getString("produit_description"),
                rs.getBigDecimal("produit_prix"), rs.getInt("produit_stock"), rs.getString("produit_image"),
                StatutProduit.fromValeurBdd(rs.getString("produit_statut")), categorie);
        return new LigneCommande(
                rs.getInt("id"), rs.getInt("quantite"), rs.getBigDecimal("prix_unitaire"),
                rs.getBigDecimal("montant_ligne"), rs.getInt("commande_id"), produit);
    }

    /** Un produit et sa quantité totale vendue — résultat des statistiques de ventes. */
    public static class ProduitVendu {
        public final int produitId;
        public final String nom;
        public final long quantiteVendue;

        public ProduitVendu(int produitId, String nom, long quantiteVendue) {
            this.produitId = produitId;
            this.nom = nom;
            this.quantiteVendue = quantiteVendue;
        }
    }
}