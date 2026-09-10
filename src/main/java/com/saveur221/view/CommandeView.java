package com.saveur221.view;

import com.saveur221.entities.Commande;
import com.saveur221.entities.LigneCommande;
import com.saveur221.enums.StatutCommande;

import java.util.List;

import static com.saveur221.view.ConsoleUtils.*;

/** Vue pure : affichage et saisie uniquement. Ne connaît pas CommandeService. */
public class CommandeView {

    public String afficherMenu() {
        titre("Gestion des commandes");
        System.out.println("1. Afficher toutes les commandes");
        System.out.println("2. Filtrer par statut");
        System.out.println("3. Voir les commandes en cours");
        System.out.println("4. Consulter le détail d'une commande");
        System.out.println("5. Faire avancer le statut d'une commande");
        System.out.println("6. Annuler une commande");
        System.out.println("0. Retour au menu principal");
        return lireTexte("Votre choix");
    }

    public int lireId(String label) {
        return lireEntier(label);
    }

    public StatutCommande choisirStatut() {
        System.out.println("Statuts : 1.EN_ATTENTE  2.EN_PREPARATION  3.PRETE  4.RETIREE  5.ANNULEE");
        String choix = lireTexte("Votre choix");
        return switch (choix) {
            case "1" -> StatutCommande.EN_ATTENTE;
            case "2" -> StatutCommande.EN_PREPARATION;
            case "3" -> StatutCommande.PRETE;
            case "4" -> StatutCommande.RETIREE;
            case "5" -> StatutCommande.ANNULEE;
            default -> null;
        };
    }

    public boolean demanderConfirmationAnnulation(int id, StatutCommande statutActuel) {
        String reponse = lireTexte(
            "Confirmer l'annulation de la commande #" + id + " (statut actuel : " + statutActuel + ") ? (o/n)");
        return reponse.equalsIgnoreCase("o");
    }

    public void afficherListe(List<Commande> commandes) {
        sousTitre(commandes.size() + " commande(s)");
        if (commandes.isEmpty()) {
            System.out.println("Aucune commande trouvée.");
            return;
        }
        for (Commande c : commandes) {
            System.out.println(c);
        }
    }

    public void afficherDetail(Commande commande) {
        sousTitre("Commande #" + commande.getId());
        System.out.println(commande);
        System.out.println("Lignes :");
        for (LigneCommande l : commande.getLignes()) {
            System.out.println("    " + l);
        }
    }

    public void afficherStatutActuel(StatutCommande statut) {
        System.out.println("Statut actuel : " + statut);
    }

    public void afficherSucces(String message) { succes(message); }
    public void afficherErreur(String message) { erreur(message); }
    public void afficherMessage(String message) { System.out.println(message); }
    public void pause() { ConsoleUtils.pause(); }
}