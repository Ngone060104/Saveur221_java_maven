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
        return lireTexte("Email");
    }

    public String lireMotDePasse() {
        return lireTexte("Mot de passe");
    }

    public void afficherBienvenue(Utilisateur utilisateur) {
        succes("Bienvenue, " + utilisateur.getNomComplet() + " (" + utilisateur.getRole() + ")");
        pause();
    }

    public void afficherErreur(String message) {
        erreur(message);
        System.out.println();
    }
}