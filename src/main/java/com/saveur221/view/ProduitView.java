package com.saveur221.view;

import com.saveur221.entities.Categorie;
import com.saveur221.entities.Produit;

import java.math.BigDecimal;
import java.util.List;

import static com.saveur221.view.ConsoleUtils.*;

public class ProduitView {

    /** Simple porteur de données saisies — pas de logique métier. */
    public record SaisieProduit(String nom, String description, BigDecimal prix,
                                 Integer stock, String image, int categorieId) {}

    public String afficherMenu() {
        titre("Gestion des produits");
        System.out.println("1. Afficher tous les produits");
        System.out.println("2. Rechercher un produit par nom");
        System.out.println("3. Filtrer par catégorie");
        System.out.println("4. Voir les produits disponibles / indisponibles");
        System.out.println("5. Ajouter un produit");
        System.out.println("6. Modifier un produit");
        System.out.println("7. Supprimer un produit");
        System.out.println("0. Retour au menu principal");
        // MODIFICATION : Évite une validation vide pour le choix du menu
        return lireTexteObligatoire("Votre choix");
    }

    public String lireMotCleRecherche() {
        return lireTexteObligatoire("Nom (ou partie du nom) à rechercher");
    }

    public int lireId(String label) {
        return lireEntier(label);
    }

    public boolean demanderDisponibles() {
        while (true) {
            String reponse = lireTexteObligatoire("Afficher les produits (d)isponibles ou (i)ndisponibles ?");
            if (reponse.equalsIgnoreCase("d")) return true;
            if (reponse.equalsIgnoreCase("i")) return false;
            System.out.println("  ⚠ Choix invalide. Veuillez saisir 'd' ou 'i'.");
        }
    }

    public void afficherCategoriesDisponibles(List<Categorie> categories) {
        System.out.println("Catégories disponibles :");
        for (Categorie c : categories) {
            System.out.println("  #" + c.getId() + " " + c.getLibelle());
        }
    }

    public SaisieProduit saisirNouveauProduit(List<Categorie> categories) {
        sousTitre("Ajouter un produit");
        // MODIFICATION : Utilisation de lireAlphabetiqueObligatoire pour forcer des lettres dans le nom
        String nom = lireAlphabetiqueObligatoire("Nom");
        String description = lireTexte("Description (optionnel)");
        
        // MODIFICATION : Force un prix strictement supérieur à 0
        BigDecimal prix;
        while (true) {
            prix = lireMontant("Prix");
            if (prix.compareTo(BigDecimal.ZERO) > 0) break;
            System.out.println("  ⚠ Le prix doit être strictement supérieur à 0.");
        }

        // MODIFICATION : Force un stock positif ou nul (>= 0)
        int stock;
        while (true) {
            stock = lireEntier("Quantité en stock initiale");
            if (stock >= 0) break;
            System.out.println("  ⚠ Le stock initial ne peut pas être négatif.");
        }

        String image = lireTexte("Nom du fichier image (optionnel)");
        afficherCategoriesDisponibles(categories);
        int categorieId = lireEntier("Id de la catégorie");
        
        return new SaisieProduit(nom, description.isBlank() ? null : description, prix, stock,
                image.isBlank() ? null : image, categorieId);
    }

    public SaisieProduit saisirModificationProduit(Produit existant, List<Categorie> categories) {
        sousTitre("Modifier un produit");
        System.out.println("Actuel : " + existant);
        // MODIFICATION : Utilisation de lireAlphabetiqueObligatoire pour le nouveau nom
        String nom = lireAlphabetiqueObligatoire("Nouveau nom");
        String description = lireTexte("Nouvelle description (optionnel)");
        
        // MODIFICATION : Force un prix strictement supérieur à 0
        BigDecimal prix;
        while (true) {
            prix = lireMontant("Nouveau prix");
            if (prix.compareTo(BigDecimal.ZERO) > 0) break;
            System.out.println("  ⚠ Le prix doit être strictement supérieur à 0.");
        }

        String image = lireTexte("Nouveau nom de fichier image (optionnel)");
        afficherCategoriesDisponibles(categories);
        int categorieId = lireEntier("Id de la catégorie");
        
        return new SaisieProduit(nom, description.isBlank() ? null : description, prix, null,
                image.isBlank() ? null : image, categorieId);
    }

    public boolean demanderConfirmationSuppression(String nom) {
        return lireTexteObligatoire("Confirmer la suppression de \"" + nom + "\" ? (o/n)").equalsIgnoreCase("o");
    }

    public void afficherListe(List<Produit> produits) {
        sousTitre(produits.size() + " produit(s)");
        if (produits.isEmpty()) {
            System.out.println("Aucun produit trouvé.");
            return;
        }
        for (Produit p : produits) {
            System.out.printf("#%-4d %-25s %-12s %8s F  stock:%-4d %s%n",
                    p.getId(), p.getNom(), "[" + p.getCategorie().getLibelle() + "]",
                    p.getPrix(), p.getStock(), p.getStatut().getValeurBdd());
        }
    }

    public void afficherSucces(String message) { succes(message); }
    public void afficherErreur(String message) { erreur(message); }
    public void afficherMessage(String message) { System.out.println(message); }
}
