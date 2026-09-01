package com.saveur221;

import com.saveur221.config.Session;
import com.saveur221.entities.Utilisateur;
import com.saveur221.exceptions.AuthentificationException;
import com.saveur221.service.AuthService;
import com.saveur221.view.LoginView;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        AuthService authService = new AuthService();
        LoginView loginView = new LoginView();

        Utilisateur utilisateur = seConnecter(authService, loginView);
        if (utilisateur == null) {
            System.out.println("\nÀ bientôt !");
            return;
        }

        System.out.println("\nConnexion réussie, on branchera le menu principal ensuite.");
    }

    /** Boucle de connexion : Main orchestre View (saisie/affichage) et Service (logique). */
    private static Utilisateur seConnecter(AuthService authService, LoginView loginView) {
        loginView.afficherEnTete();

        while (true) {
            String email = loginView.lireEmail();
            if (email.equalsIgnoreCase("q")) {
                return null;
            }
            String motDePasse = loginView.lireMotDePasse();
            if (motDePasse.equalsIgnoreCase("q")) {
                return null;
            }

            try {
                Utilisateur utilisateur = authService.connexion(email, motDePasse);
                Session.connecter(utilisateur);
                loginView.afficherBienvenue(utilisateur);
                return utilisateur;
            } catch (AuthentificationException e) {
                loginView.afficherErreur(e.getMessage());
            }
        }
    }
}