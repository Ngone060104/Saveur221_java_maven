package com.saveur221.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Aucun champ "statut" ici : conformément à la correction apportée au
 * diagramme de classes, le statut de paiement d'une commande (impayée /
 * partiellement payée / soldée) est CALCULÉ, jamais stocké. Voir
 * {@link com.saveur221.service.PaiementService#getStatutPaiement(Commande)}
 * qui reproduit exactement la logique de la vue SQL "vue_statut_paiement"
 * partagée avec le module PHP.
 */
public class Paiement {

    private Integer id;
    private BigDecimal montant;
    private LocalDateTime datePaiement;
    private Integer commandeId;

    public Paiement() {
    }

    public Paiement(Integer id, BigDecimal montant, LocalDateTime datePaiement, Integer commandeId) {
        this.id = id;
        this.montant = montant;
        this.datePaiement = datePaiement;
        this.commandeId = commandeId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public LocalDateTime getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(LocalDateTime datePaiement) {
        this.datePaiement = datePaiement;
    }

    public Integer getCommandeId() {
        return commandeId;
    }

    public void setCommandeId(Integer commandeId) {
        this.commandeId = commandeId;
    }

    @Override
    public String toString() {
        return String.format("Paiement #%d : %s F le %s", id, montant, datePaiement);
    }
}
