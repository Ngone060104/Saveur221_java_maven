package com.saveur221.entities;

import java.time.LocalDateTime;

/**
 * Classe mère du diagramme de classes : porte les champs communs à tout
 * compte (personnel interne ET clients, ceux-ci héritant via {@link com.saveur221.entities.Client}
 * — même schéma relationnel que le module PHP : table utilisateurs).
 *
 * Pour le Module A (console), seuls les comptes GERANT et ADMIN se connectent ;
 * la sous-classe Client existe surtout pour manipuler des commandes/avis
 * créés côté web, si le gérant a besoin de les consulter.
 */
public class Utilisateur {

    private Integer id;
    private String nom;
    private String prenom;
    private String email;
    private String mdp; // hash bcrypt, jamais en clair
    private boolean actif;
    private LocalDateTime dateCreation;
    private Role role;

    public Utilisateur() {
    }

    public Utilisateur(Integer id, String nom, String prenom, String email, String mdp,
                        boolean actif, LocalDateTime dateCreation, Role role) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.mdp = mdp;
        this.actif = actif;
        this.dateCreation = dateCreation;
        this.role = role;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMdp() {
        return mdp;
    }

    public void setMdp(String mdp) {
        this.mdp = mdp;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    @Override
    public String toString() {
        return String.format("#%d %s (%s) - %s", id, getNomComplet(), email, role);
    }
}
