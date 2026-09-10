package com.saveur221.view;

import com.saveur221.entities.Utilisateur;

import static com.saveur221.view.ConsoleUtils.*;

/** Vue pure : affiche, lit la saisie, ne connaît pas AuthService. */
public class LoginView {

    public void afficherEnTete() {
        titre("SAVEUR 221 — Connexion");
        System.out.println("Application réservée au personnel interne (gérant / administrateur).");
        System.out.println("Tapez \"q\" à tout moment pour quitter.\n");
    }

    public String lireEmail() {
        while (true) {
            String email = lireTexteObligatoire("Email");
            
            // Permet de quitter si l'utilisateur saisit "q" ou "Q"
            if (email.equalsIgnoreCase("q")) {
                return email;
            }
            
            // Vérifie la validité du format de l'adresse email
            String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
            if (email.matches(emailRegex)) {
                return email;
            }
            System.out.println("  ⚠ Format d'email invalide (ex: nom@domaine.com) ou tapez 'q' pour quitter.");
        }
    }

    public String lireMotDePasse() {
        // MODIFICATION : Utilisation de lireTexteObligatoire pour éviter une saisie vide
        return lireTexteObligatoire("Mot de passe");
    }

    public void afficherBienvenue(Utilisateur utilisateur) {
        succes("Bienvenue, " + utilisateur.getNomComplet() + " (" + utilisateur.getRole() + ")");
    }

    public void afficherErreur(String message) {
        erreur(message);
        System.out.println();
    }
}
