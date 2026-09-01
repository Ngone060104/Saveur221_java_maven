package com.saveur221.service;

import com.saveur221.entities.Utilisateur;
import com.saveur221.exceptions.AuthentificationException;
import com.saveur221.repository.UtilisateurRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

public class AuthService {

    private final UtilisateurRepository utilisateurRepository;

    public AuthService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    public AuthService() {
        this(new UtilisateurRepository());
    }

    public Utilisateur connexion(String email, String motDePasseSaisi) {
        if (email == null || email.isBlank() || motDePasseSaisi == null || motDePasseSaisi.isBlank()) {
            throw new AuthentificationException("Email et mot de passe sont obligatoires.");
        }

        Optional<Utilisateur> optUtilisateur = utilisateurRepository.trouverParEmail(email.trim().toLowerCase());
        if (optUtilisateur.isEmpty()) {
            throw new AuthentificationException("Email ou mot de passe incorrect.");
        }

        Utilisateur utilisateur = optUtilisateur.get();

        if (!verifierMotDePasse(motDePasseSaisi, utilisateur.getMdp())) {
            throw new AuthentificationException("Email ou mot de passe incorrect.");
        }

        if (!utilisateur.isActif()) {
            throw new AuthentificationException("Ce compte a été désactivé. Contactez un administrateur.");
        }

        if (!utilisateur.getRole().isAdmin() && !utilisateur.getRole().isGerant()) {
            throw new AuthentificationException("Ce compte n'a pas accès à l'application console.");
        }

        return utilisateur;
    }

    private boolean verifierMotDePasse(String saisi, String hashStocke) {
        if (hashStocke == null || hashStocke.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(saisi, hashStocke);
        } catch (Exception e) {
            // hash corrompu/en clair (données de test) : jamais planter, juste refuser
            return false;
        }
    }

    public static String hacherMotDePasse(String motDePasseClair) {
        return BCrypt.hashpw(motDePasseClair, BCrypt.gensalt());
    }
}