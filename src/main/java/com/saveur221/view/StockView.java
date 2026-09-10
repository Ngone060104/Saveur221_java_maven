package com.saveur221.view;

import com.saveur221.entities.Produit;

import java.util.List;

import static com.saveur221.view.ConsoleUtils.*;

/** Vue pure : affichage et saisie uniquement. Ne connaît pas StockService. */
public class StockView {

    public String afficherMenu(int seuilActuel) {
        titre("Gestion du stock");
        System.out.println("1. Consulter le stock de tous les produits");
        System.out.println("2. Approvisionner un produit");
        System.out.println("3. Définir le seuil d'alerte (actuel : " + seuilActuel + ")");
        System.out.println("4. Voir les produits à stock faible");
        System.out.println("5. Voir les produits en rupture");
        System.out.println("0. Retour au menu principal");
        // MODIFICATION : Empêche une validation vide pour le choix du menu
        return lireTexteObligatoire("Votre choix");
    }

    public int lireIdProduit() {
        // Géré par lireEntier (redemande si vide ou invalide)
        return lireEntier("Id du produit");
    }

    public int lireQuantiteAAjouter() {
        // MODIFICATION : Force l'ajout d'une quantité strictement positive (> 0)
        while (true) {
            int quantite = lireEntier("Quantité à ajouter");
            if (quantite > 0) {
                return quantite;
            }
            System.out.println("  ⚠ La quantité à ajouter doit être strictement supérieure à 0.");
        }
    }

    public int lireNouveauSeuil() {
        // MODIFICATION : Force un seuil d'alerte positif ou nul (>= 0)
        while (true) {
            int seuil = lireEntier("Nouveau seuil d'alerte (produits à ce niveau ou en dessous = stock faible)");
            if (seuil >= 0) {
                return seuil;
            }
            System.out.println("  ⚠ Le seuil d'alerte ne peut pas être négatif.");
        }
    }

    public void afficherListe(List<Produit> produits) {
        sousTitre(produits.size() + " produit(s)");
        if (produits.isEmpty()) {
            System.out.println("Aucun produit trouvé.");
            return;
        }
        for (Produit p : produits) {
            System.out.printf("#%-4d %-25s stock:%-4d %s%n",
                    p.getId(), p.getNom(), p.getStock(), p.getStatut().getValeurBdd());
        }
    }

    public void afficherSucces(String message) { succes(message); }
    public void afficherErreur(String message) { erreur(message); }
}
