package com.saveur221.view;

import com.saveur221.entities.Utilisateur;

import java.util.List;

import static com.saveur221.view.ConsoleUtils.*;

/** Vue pure : affichage et saisie uniquement. Ne connaît pas UtilisateurService. */
public class UtilisateurView {

    /** Simple porteur de données saisies — pas de logique métier. */
    public record SaisieUtilisateur(String nom, String prenom, String email, String motDePasse, String role) {}

    public String afficherMenu() {
        titre("Gestion des utilisateurs internes (ADMIN)");
        System.out.println("1. Afficher les utilisateurs");
        System.out.println("2. Rechercher un utilisateur");
        System.out.println("3. Ajouter un utilisateur");
        System.out.println("4. Modifier un utilisateur");
        System.out.println("5. Activer / désactiver un utilisateur");
        System.out.println("6. Supprimer un utilisateur");
        System.out.println("0. Retour au menu principal");
        return lireTexte("Votre choix");
    }

    public String lireMotCleRecherche() {
        return lireTexteObligatoire("Nom, prénom ou email à rechercher");
    }

    public int lireId(String label) {
        return lireEntier(label);
    }

    public SaisieUtilisateur saisirNouvelUtilisateur() {
        sousTitre("Ajouter un utilisateur");
        String nom = lireTexteObligatoire("Nom");
        String prenom = lireTexteObligatoire("Prénom");
        String email = lireTexteObligatoire("Email");
        String motDePasse = lireTexteObligatoire("Mot de passe (min. 6 caractères)");
        String role = lireTexteObligatoire("Rôle (ADMIN ou GERANT)");
        return new SaisieUtilisateur(nom, prenom, email, motDePasse, role);
    }

    public SaisieUtilisateur saisirModificationUtilisateur(Utilisateur existant) {
        sousTitre("Modifier un utilisateur");
        System.out.println("Actuel : " + existant);
        String nom = lireTexteObligatoire("Nouveau nom");
        String prenom = lireTexteObligatoire("Nouveau prénom");
        String email = lireTexteObligatoire("Nouvel email");
        String role = lireTexteObligatoire("Nouveau rôle (ADMIN ou GERANT)");
        return new SaisieUtilisateur(nom, prenom, email, null, role);
    }

    public boolean demanderConfirmation(String message) {
        return lireTexte(message + " (o/n)").equalsIgnoreCase("o");
    }

    public void afficherListe(List<Utilisateur> utilisateurs) {
        sousTitre(utilisateurs.size() + " utilisateur(s)");
        if (utilisateurs.isEmpty()) {
            System.out.println("Aucun utilisateur trouvé.");
            return;
        }
        for (Utilisateur u : utilisateurs) {
            System.out.printf("#%-4d %-25s %-30s %-8s %s%n",
                    u.getId(), u.getNomComplet(), u.getEmail(), u.getRole(),
                    u.isActif() ? "actif" : "désactivé");
        }
    }

    public void afficherSucces(String message) { succes(message); }
    public void afficherErreur(String message) { erreur(message); }
    public void afficherMessage(String message) { System.out.println(message); }
    public void pause() { ConsoleUtils.pause(); }
}