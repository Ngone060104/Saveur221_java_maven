package com.saveur221.entities;

public class Role {

    private Integer id;
    private String libelle;

    public Role() {
    }

    public Role(Integer id, String libelle) {
        this.id = id;
        this.libelle = libelle;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(libelle);
    }

    public boolean isGerant() {
        return "GERANT".equalsIgnoreCase(libelle);
    }

    @Override
    public String toString() {
        return libelle;
    }
}
