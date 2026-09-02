package com.saveur221.view;

import com.saveur221.entities.Categorie;

import java.util.List;

import static com.saveur221.view.ConsoleUtils.*;

/** Vue pure : affichage et saisie uniquement. Ne connaît pas CategorieService. */
public class CategorieView {

    public String afficherMenu() {
        titre("Gestion des catégories");
        System.out.println("1. Afficher toutes les catégories");
        System.out.println("2. Rechercher une catégorie");
        System.out.println("3. Ajouter une catégorie");
        System.out.println("4. Modifier une catégorie");
        System.out.println("5. Supprimer une catégorie");
        System.out.println("0. Retour au menu principal");
        return lireTexte("Votre choix");
    }

    public String lireMotCleRecherche() {
        return lireTexteObligatoire("Libellé (ou partie du libellé) à rechercher");
    }

    public int lireId(String label) {
        return lireEntier(label);
    }

    public Categorie saisirNouvelleCategorie() {
        sousTitre("Ajouter une catégorie");
        String libelle = lireTexteObligatoire("Libellé");
        String description = lireTexte("Description (optionnel)");
        return new Categorie(null, libelle, description.isBlank() ? null : description);
    }

    public Categorie saisirModificationCategorie(Categorie existante) {
        sousTitre("Modifier une catégorie");
        System.out.println("Actuel : " + existante.getLibelle() + " — "
                + (existante.getDescription() != null ? existante.getDescription() : "(sans description)"));
        String libelle = lireTexteObligatoire("Nouveau libellé");
        String description = lireTexte("Nouvelle description (optionnel)");
        return new Categorie(existante.getId(), libelle, description.isBlank() ? null : description);
    }

    public boolean demanderConfirmationSuppression(String libelle) {
        String reponse = lireTexte("Confirmer la suppression de \"" + libelle + "\" ? (o/n)");
        return reponse.equalsIgnoreCase("o");
    }

    public void afficherListe(List<Categorie> categories) {
        sousTitre(categories.size() + " catégorie(s)");
        if (categories.isEmpty()) {
            System.out.println("Aucune catégorie trouvée.");
            return;
        }
        for (Categorie c : categories) {
            System.out.printf("#%-3d %-20s %s%n", c.getId(), c.getLibelle(),
                    c.getDescription() != null ? c.getDescription() : "");
        }
    }

    public void afficherSucces(String message) {
        succes(message);
    }

    public void afficherErreur(String message) {
        erreur(message);
    }

    public void afficherMessage(String message) {
        System.out.println(message);
    }

    public void pause() {
        ConsoleUtils.pause();
    }
}