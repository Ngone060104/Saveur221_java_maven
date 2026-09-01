package com.saveur221;

import com.saveur221.entities.Utilisateur;
import com.saveur221.service.AuthService;
import com.saveur221.view.LoginView;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        Utilisateur utilisateur = new LoginView(new AuthService()).demarrer();
        if (utilisateur == null) {
            System.out.println("\nÀ bientôt !");
            return;
        }
        System.out.println("\nConnexion réussie, on branchera le menu principal ensuite.");
    }
}