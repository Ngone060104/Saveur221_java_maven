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
        // MODIFICATION : Évite une validation vide pour le choix du menu
        return lireTexteObligatoire("Votre choix");
    }

    public String lireMotCleRecherche() {
        return lireTexteObligatoire("Nom, prénom ou email à rechercher");
    }

    public int lireId(String label) {
        return lireEntier(label);
    }

    public SaisieUtilisateur saisirNouvelUtilisateur() {
        sousTitre("Ajouter un utilisateur");
        
        // MODIFICATION : Forcer des caractères alphabétiques pour l'identité
        String nom = lireAlphabetiqueObligatoire("Nom");
        String prenom = lireAlphabetiqueObligatoire("Prénom");
        
        // MODIFICATION : Validation stricte du format email (ex: nom@domaine.com)
        String email = lireEmailObligatoire("Email");
        
        // MODIFICATION : Validation de la contrainte métier sur le mot de passe (min. 6 caractères)
        String motDePasse;
        while (true) {
            motDePasse = lireTexteObligatoire("Mot de passe (min. 6 caractères)");
            if (motDePasse.length() >= 6) {
                break;
            }
            System.out.println("  ⚠ Le mot de passe doit comporter au moins 6 caractères.");
        }
        
        // MODIFICATION : Force la saisie d'un rôle valide de l'application
        String role;
        while (true) {
            role = lireTexteObligatoire("Rôle (ADMIN ou GERANT)").toUpperCase();
            if (role.equals("ADMIN") || role.equals("GERANT")) {
                break;
            }
            System.out.println(" Rôle invalide. Seuls 'ADMIN' ou 'GERANT' sont acceptés.");
        }
        
        return new SaisieUtilisateur(nom, prenom, email, motDePasse, role);
    }

    public SaisieUtilisateur saisirModificationUtilisateur(Utilisateur existant) {
        sousTitre("Modifier un utilisateur");
        System.out.println("Actuel : " + existant);
        
        // MODIFICATION : Forcer des caractères alphabétiques
        String nom = lireAlphabetiqueObligatoire("Nouveau nom");
        String prenom = lireAlphabetiqueObligatoire("Nouveau prénom");
        
        // MODIFICATION : Validation stricte du format de l'email
        String email = lireEmailObligatoire("Nouvel email");
        
        // MODIFICATION : Force la saisie d'un rôle valide de l'application
        String role;
        while (true) {
            role = lireTexteObligatoire("Nouveau rôle (ADMIN ou GERANT)").toUpperCase();
            if (role.equals("ADMIN") || role.equals("GERANT")) {
                break;
            }
            System.out.println(" Rôle invalide. Seuls 'ADMIN' ou 'GERANT' sont acceptés.");
        }
        
        return new SaisieUtilisateur(nom, prenom, email, null, role);
    }

    public boolean demanderConfirmation(String message) {
        // MODIFICATION : Utilisation de lireTexteObligatoire pour éviter une confirmation par Entrée brute
        return lireTexteObligatoire(message + " (o/n)").equalsIgnoreCase("o");
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
}
