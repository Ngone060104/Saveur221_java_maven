package com.saveur221.entities;

import java.time.LocalDateTime;

/**
 * Hérite de {@link Utilisateur}, conformément au diagramme de classes
 * (généralisation Client -> UTILISATEUR). Ajoute telephone et adresse.
 * Correspond en base à la table "clients" (id = FK vers utilisateurs.id).
 */
public class Client extends Utilisateur {

    private String telephone;
    private String adresse;

    public Client() {
        super();
    }

    public Client(Integer id, String nom, String prenom, String email, String mdp,
                  boolean actif, LocalDateTime dateCreation, Role role,
                  String telephone, String adresse) {
        super(id, nom, prenom, email, mdp, actif, dateCreation, role);
        this.telephone = telephone;
        this.adresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
}
