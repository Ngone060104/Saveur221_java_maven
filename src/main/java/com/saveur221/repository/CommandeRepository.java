package com.saveur221.repository;

import com.saveur221.config.DatabaseConfig;
import com.saveur221.entities.Client;
import com.saveur221.entities.Commande;
import com.saveur221.entities.Role;
import com.saveur221.enums.StatutCommande;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Le Module A Console ne crée pas de commandes (rôle du site web PHP) : le
 * gérant les consulte, change leur statut, et peut les annuler. La
 * restitution du stock à l'annulation est gérée par le trigger PostgreSQL
 * trg_commande_annulation, partagé avec le module PHP.
 */
public class CommandeRepository {

    private static final String SELECT_BASE =
        "SELECT co.id, co.date_commande, co.montant_total, co.statut, " +
        "       cl.id AS client_id, u.nom AS client_nom, u.prenom AS client_prenom, " +
        "       u.email AS client_email, u.mdp AS client_mdp, u.actif AS client_actif, " +
        "       u.date_creation AS client_date_creation, " +
        "       r.id AS role_id, r.libelle AS role_libelle, " +
        "       cl.telephone AS client_telephone, cl.adresse AS client_adresse " +
        "FROM commandes co " +
        "JOIN clients cl ON cl.id = co.client_id " +
        "JOIN utilisateurs u ON u.id = cl.id " +
        "JOIN roles r ON r.id = u.role_id ";

    public List<Commande> lister() {
        return executerListe(SELECT_BASE + "ORDER BY co.date_commande DESC", ps -> {});
    }

    public Optional<Commande> trouverParId(int id) {
        String sql = SELECT_BASE + "WHERE co.id = ?";
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapper(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de la commande #" + id, e);
        }
        return Optional.empty();
    }

    public List<Commande> filtrerParStatut(StatutCommande statut) {
        return executerListe(SELECT_BASE + "WHERE co.statut = ?::statut_commande_enum ORDER BY co.date_commande DESC",
                ps -> ps.setString(1, statut.name()));
    }

    public List<Commande> listerEnCours() {
        return executerListe(
                SELECT_BASE + "WHERE co.statut IN ('EN_ATTENTE','EN_PREPARATION','PRETE') ORDER BY co.date_commande",
                ps -> {});
    }

    public void changerStatut(int id, StatutCommande nouveauStatut) {
        String sql = "UPDATE commandes SET statut = ?::statut_commande_enum WHERE id = ?";
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, nouveauStatut.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du changement de statut de la commande #" + id, e);
        }
    }

    // --- Statistiques -------------------------------------------------------

    /** Chiffre d'affaires depuis une date donnée, commandes ANNULEE exclues. */
    public BigDecimal chiffreAffaires(LocalDateTime depuis) {
        String sql = "SELECT COALESCE(SUM(montant_total), 0) FROM commandes " +
                     "WHERE date_commande >= ? AND statut <> 'ANNULEE'";
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(depuis));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du calcul du chiffre d'affaires", e);
        }
    }

    public int compterCommandes() {
        return compter("SELECT COUNT(*) FROM commandes");
    }

    public int compterCommandesEnCours() {
        return compter("SELECT COUNT(*) FROM commandes WHERE statut IN ('EN_ATTENTE','EN_PREPARATION','PRETE')");
    }

    public int compterParStatut(StatutCommande statut) {
        String sql = "SELECT COUNT(*) FROM commandes WHERE statut = ?::statut_commande_enum";
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, statut.name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des commandes par statut", e);
        }
    }

    private int compter(String sql) {
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors d'un comptage de commandes", e);
        }
    }

    // --- utilitaires internes -----------------------------------------------

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<Commande> executerListe(String sql, Binder binder) {
        List<Commande> resultat = new ArrayList<>();
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) resultat.add(mapper(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des commandes", e);
        }
        return resultat;
    }

    private Commande mapper(ResultSet rs) throws SQLException {
        Role role = new Role(rs.getInt("role_id"), rs.getString("role_libelle"));
        Timestamp dateCreationClient = rs.getTimestamp("client_date_creation");
        Client client = new Client(
                rs.getInt("client_id"), rs.getString("client_nom"), rs.getString("client_prenom"),
                rs.getString("client_email"), rs.getString("client_mdp"), rs.getBoolean("client_actif"),
                dateCreationClient != null ? dateCreationClient.toLocalDateTime() : null, role,
                rs.getString("client_telephone"), rs.getString("client_adresse"));

        Timestamp dateCommande = rs.getTimestamp("date_commande");
        return new Commande(
                rs.getInt("id"),
                dateCommande != null ? dateCommande.toLocalDateTime() : null,
                rs.getBigDecimal("montant_total"),
                StatutCommande.valueOf(rs.getString("statut")),
                client);
    }
}