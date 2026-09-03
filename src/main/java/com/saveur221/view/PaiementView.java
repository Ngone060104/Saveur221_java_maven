package com.saveur221.view;

import com.saveur221.entities.Paiement;
import com.saveur221.repository.PaiementRepository.StatutPaiementInfo;

import java.math.BigDecimal;
import java.util.List;

import static com.saveur221.view.ConsoleUtils.*;

/** Vue pure : affichage et saisie uniquement. Ne connaît pas PaiementService. */
public class PaiementView {

    public String afficherMenu() {
        titre("Gestion des paiements");
        System.out.println("1. Afficher les paiements d'une commande");
        System.out.println("2. Voir les commandes impayées / partiellement payées");
        System.out.println("3. Enregistrer un paiement");
        System.out.println("0. Retour au menu principal");
        return lireTexte("Votre choix");
    }

    public int lireIdCommande() {
        return lireEntier("Id de la commande");
    }

    public BigDecimal lireMontantRecu() {
        return lireMontant("Montant reçu");
    }

    public void afficherPaiementsEtStatut(List<Paiement> paiements, StatutPaiementInfo info) {
        sousTitre("Paiements de la commande #" + info.commandeId);
        if (paiements.isEmpty()) {
            System.out.println("Aucun paiement enregistré.");
        } else {
            for (Paiement p : paiements) {
                System.out.println("  " + p);
            }
        }
        System.out.printf("%nTotal commande : %s F  |  Payé : %s F  |  Restant : %s F  |  Statut : %s%n",
                info.montantTotal, info.montantPaye, info.montantRestant, info.statutPaiement);
    }

    public void afficherImpayeesEtPartielles(List<StatutPaiementInfo> infos) {
        sousTitre(infos.size() + " commande(s) impayée(s) ou partiellement payée(s)");
        if (infos.isEmpty()) {
            System.out.println("Toutes les commandes sont soldées.");
            return;
        }
        for (StatutPaiementInfo info : infos) {
            System.out.printf("Commande #%-4d  total:%8s F  payé:%8s F  restant:%8s F  [%s]%n",
                    info.commandeId, info.montantTotal, info.montantPaye, info.montantRestant, info.statutPaiement);
        }
    }

    public void afficherMontantRestant(BigDecimal montantRestant) {
        System.out.println("Montant restant à payer : " + montantRestant + " F");
    }

    public boolean estDejaSoldee(BigDecimal montantRestant) {
        return montantRestant.compareTo(BigDecimal.ZERO) <= 0;
    }

    public void afficherSucces(String message) { succes(message); }
    public void afficherErreur(String message) { erreur(message); }
    public void afficherMessage(String message) { System.out.println(message); }
    public void pause() { ConsoleUtils.pause(); }
}