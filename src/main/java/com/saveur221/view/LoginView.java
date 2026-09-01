package com.saveur221.view;

import com.saveur221.config.Session;
import com.saveur221.entities.Utilisateur;
import com.saveur221.exceptions.AuthentificationException;
import com.saveur221.service.AuthService;

import static com.saveur221.view.ConsoleUtils.*;

public class LoginView {

    private final AuthService authService;

    public LoginView(AuthService authService) {
        this.authService = authService;
    }

    public Utilisateur demarrer() {
        titre("SAVEUR 221 — Connexion");
        System.out.println("Application reservee au personnel interne (gerant / administrateur).");
        System.out.println("Tapez \"q\" à tout moment pour quitter.\n");

        while (true) {
            String email = lireTexte("Email");
            if (email.equalsIgnoreCase("q")) {
                return null;
            }
            String motDePasse = lireTexte("Mot de passe");
            if (motDePasse.equalsIgnoreCase("q")) {
                return null;
            }

            try {
                Utilisateur utilisateur = authService.connexion(email, motDePasse);
                Session.connecter(utilisateur);
                succes("Bienvenue, " + utilisateur.getNomComplet() + " (" + utilisateur.getRole() + ")");
                pause();
                return utilisateur;
            } catch (AuthentificationException e) {
                erreur(e.getMessage());
                System.out.println();
            }
        }
    }
}