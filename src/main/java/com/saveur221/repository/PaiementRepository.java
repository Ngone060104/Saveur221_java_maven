package com.saveur221.repository;

import com.saveur221.config.DatabaseConfig;
import com.saveur221.entities.Paiement;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaiementRepository {

    public List<Paiement> listerParCommande(int commandeId) {
        String sql = "SELECT id, montant, date_paiement, commande_id FROM paiements " +
                     "WHERE commande_id = ? ORDER BY date_paiement";
        List<Paiement> resultat = new ArrayList<>();
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, commandeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) resultat.add(mapper(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des paiements de la commande #" + commandeId, e);
        }
        return resultat;
    }

    /**
     * Interroge la vue partagée "vue_statut_paiement" (même source de vérité
     * que le module PHP) pour connaître montant payé / restant / statut calculé.
     */
    public StatutPaiementInfo getStatutPaiement(int commandeId) {
        String sql = "SELECT montant_total, montant_paye, montant_restant, statut_paiement " +
                     "FROM vue_statut_paiement WHERE commande_id = ?";
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, commandeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new StatutPaiementInfo(commandeId,
                            rs.getBigDecimal("montant_total"), rs.getBigDecimal("montant_paye"),
                            rs.getBigDecimal("montant_restant"), rs.getString("statut_paiement"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du calcul du statut de paiement de la commande #" + commandeId, e);
        }
        throw new com.saveur221.exceptions.RessourceIntrouvableException("Aucune commande avec l'id #" + commandeId);
    }

    public List<StatutPaiementInfo> listerCommandesImpayeesOuPartielles() {
        String sql = "SELECT commande_id, montant_total, montant_paye, montant_restant, statut_paiement " +
                     "FROM vue_statut_paiement WHERE statut_paiement <> 'TOTALEMENT_PAYEE' " +
                     "ORDER BY commande_id";
        List<StatutPaiementInfo> resultat = new ArrayList<>();
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultat.add(new StatutPaiementInfo(
                        rs.getInt("commande_id"), rs.getBigDecimal("montant_total"),
                        rs.getBigDecimal("montant_paye"), rs.getBigDecimal("montant_restant"),
                        rs.getString("statut_paiement")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des commandes impayées/partielles", e);
        }
        return resultat;
    }

    /**
     * Le contrôle "montant <= montant restant" (règle métier 12) est appliqué
     * par le trigger PostgreSQL trg_paiement_before_insert, partagé avec le
     * module PHP.
     */
    public Paiement creer(Paiement paiement) {
        String sql = "INSERT INTO paiements (montant, commande_id) VALUES (?, ?)";
        try (Connection cnx = DatabaseConfig.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setBigDecimal(1, paiement.getMontant());
            ps.setInt(2, paiement.getCommandeId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) paiement.setId(keys.getInt(1));
            }
            return paiement;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("dépasse le montant restant")) {
                throw new com.saveur221.exceptions.ValidationException(
                    "Le paiement dépasse le montant restant de la commande.");
            }
            throw new RuntimeException("Erreur lors de l'enregistrement du paiement", e);
        }
    }

    private Paiement mapper(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("date_paiement");
        return new Paiement(rs.getInt("id"), rs.getBigDecimal("montant"),
                ts != null ? ts.toLocalDateTime() : null, rs.getInt("commande_id"));
    }

    /** Reflète une ligne de la vue SQL vue_statut_paiement. */
    public static class StatutPaiementInfo {
        public final int commandeId;
        public final BigDecimal montantTotal;
        public final BigDecimal montantPaye;
        public final BigDecimal montantRestant;
        public final String statutPaiement;

        public StatutPaiementInfo(int commandeId, BigDecimal montantTotal, BigDecimal montantPaye,
                                   BigDecimal montantRestant, String statutPaiement) {
            this.commandeId = commandeId;
            this.montantTotal = montantTotal;
            this.montantPaye = montantPaye;
            this.montantRestant = montantRestant;
            this.statutPaiement = statutPaiement;
        }
    }
}