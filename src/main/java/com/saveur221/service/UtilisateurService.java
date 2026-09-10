package com.saveur221.service;

import com.saveur221.entities.Role;
import com.saveur221.entities.Utilisateur;
import com.saveur221.exceptions.RessourceIntrouvableException;
import com.saveur221.exceptions.ValidationException;
import com.saveur221.repository.RoleRepository;
import com.saveur221.repository.UtilisateurRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CRUD des utilisateurs internes (ADMIN, GERANT), réservé au rôle ADMIN
 * côté vue. Règles métier : email unique (règle 2), mot de passe
 * >= 6 caractères avant hash (règle 3).
 */
public class UtilisateurService {

    private static final int LONGUEUR_MDP_MINIMALE = 6;

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;

    public UtilisateurService(UtilisateurRepository utilisateurRepository, RoleRepository roleRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
    }

    public UtilisateurService() {
        this(new UtilisateurRepository(), new RoleRepository());
    }

    public List<Utilisateur> lister() {
        return utilisateurRepository.listerPersonnelInterne();
    }

    public List<Utilisateur> rechercher(String motCle) {
        return utilisateurRepository.rechercherParNomInterne(motCle);
    }

    public Utilisateur trouverParId(int id) {
        return utilisateurRepository.trouverParId(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Aucun utilisateur avec l'id #" + id));
    }

    public Utilisateur ajouter(String nom, String prenom, String email, String motDePasseClair, String roleLibelle) {
        validerNomPrenom(nom, prenom);
        validerEmailDisponible(email);
        validerMotDePasse(motDePasseClair);
        Role role = trouverRoleInterne(roleLibelle);

        String hash = AuthService.hacherMotDePasse(motDePasseClair);
        Utilisateur nouveau = new Utilisateur(null, nom.trim(), prenom.trim(), email.trim().toLowerCase(),
                hash, true, LocalDateTime.now(), role);
        return utilisateurRepository.creer(nouveau);
    }

    public void modifier(int id, String nom, String prenom, String email, String roleLibelle) {
        validerNomPrenom(nom, prenom);
        Utilisateur existant = trouverParId(id);
        if (!existant.getEmail().equalsIgnoreCase(email)) {
            validerEmailDisponible(email);
        }
        Role role = trouverRoleInterne(roleLibelle);
        existant.setNom(nom.trim());
        existant.setPrenom(prenom.trim());
        existant.setEmail(email.trim().toLowerCase());
        existant.setRole(role);
        utilisateurRepository.mettreAJour(existant);
    }

    public void changerActif(int id, boolean actif) {
        trouverParId(id);
        utilisateurRepository.changerActif(id, actif);
    }

    public void supprimer(int id) {
        trouverParId(id);
        utilisateurRepository.supprimer(id);
    }

    private Role trouverRoleInterne(String roleLibelle) {
        if (!"ADMIN".equalsIgnoreCase(roleLibelle) && !"GERANT".equalsIgnoreCase(roleLibelle)) {
            throw new ValidationException("Le rôle doit être ADMIN ou GERANT.");
        }
        return roleRepository.trouverParLibelle(roleLibelle.toUpperCase())
                .orElseThrow(() -> new ValidationException("Rôle \"" + roleLibelle + "\" introuvable en base."));
    }

    private void validerNomPrenom(String nom, String prenom) {
        if (nom == null || nom.isBlank() || prenom == null || prenom.isBlank()) {
            throw new ValidationException("Le nom et le prénom sont obligatoires.");
        }
    }

    private void validerEmailDisponible(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new ValidationException("L'email est invalide.");
        }
        if (utilisateurRepository.existeParEmail(email)) {
            throw new ValidationException("Un compte utilise déjà l'email \"" + email + "\".");
        }
    }

    private void validerMotDePasse(String motDePasse) {
        if (motDePasse == null || motDePasse.length() < LONGUEUR_MDP_MINIMALE) {
            throw new ValidationException(
                "Le mot de passe doit contenir au moins " + LONGUEUR_MDP_MINIMALE + " caractères.");
        }
    }
}