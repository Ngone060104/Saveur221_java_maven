package com.saveur221.config;

import com.saveur221.entities.Utilisateur;

/**
 * Contexte de session très simple pour une application console mono-utilisateur :
 * garde en mémoire la personne actuellement connectée le temps de l'exécution.
 */
public final class Session {

    private static Utilisateur utilisateurConnecte;

    private Session() {
    }

    public static void connecter(Utilisateur utilisateur) {
        utilisateurConnecte = utilisateur;
    }

    public static void deconnecter() {
        utilisateurConnecte = null;
    }

    public static Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public static boolean estConnecte() {
        return utilisateurConnecte != null;
    }

    public static boolean estAdmin() {
        return estConnecte() && utilisateurConnecte.getRole().isAdmin();
    }
}
