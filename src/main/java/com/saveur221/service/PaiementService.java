package com.saveur221.service;

import com.saveur221.entities.Paiement;
import com.saveur221.exceptions.ValidationException;
import com.saveur221.repository.PaiementRepository;
import com.saveur221.repository.PaiementRepository.StatutPaiementInfo;

import java.math.BigDecimal;
import java.util.List;

public class PaiementService {

    private final PaiementRepository paiementRepository;

    public PaiementService(PaiementRepository paiementRepository) {
        this.paiementRepository = paiementRepository;
    }

    public PaiementService() {
        this(new PaiementRepository());
    }

    public List<Paiement> listerParCommande(int commandeId) {
        return paiementRepository.listerParCommande(commandeId);
    }

    public StatutPaiementInfo getStatutPaiement(int commandeId) {
        return paiementRepository.getStatutPaiement(commandeId);
    }

    public List<StatutPaiementInfo> listerCommandesImpayeesOuPartielles() {
        return paiementRepository.listerCommandesImpayeesOuPartielles();
    }

    /**
     * Règle métier 12 (un paiement ne doit jamais dépasser le montant restant),
     * vérifiée ici pour un retour immédiat, et garantie en base par trigger.
     */
    public Paiement enregistrer(int commandeId, BigDecimal montant) {
        if (montant == null || montant.signum() <= 0) {
            throw new ValidationException("Le montant du paiement doit être positif.");
        }
        StatutPaiementInfo info = getStatutPaiement(commandeId);
        if (montant.compareTo(info.montantRestant) > 0) {
            throw new ValidationException(
                "Le paiement (" + montant + " F) dépasse le montant restant ("
                + info.montantRestant + " F) de la commande #" + commandeId + ".");
        }
        return paiementRepository.creer(new Paiement(null, montant, null, commandeId));
    }
}