package com.saveur221.entities;

import java.math.BigDecimal;

public class Produit {

    private Integer id;
    private String libelle;
    private String description;
    private BigDecimal prix;
    private int stock;
    private String image;
    private StatutProduit statut;
    private Categorie categorie;

    // Règle métier : en dessous de ce seuil, le produit est signalé "stock faible"
    // (non stocké en base dans le diagramme fourni : seuil global côté service).
    public static final int SEUIL_STOCK_FAIBLE_PAR_DEFAUT = 5;

    public Produit() {
    }

    public Produit(Integer id, String libelle, String description, BigDecimal prix, int stock,
                   String image, StatutProduit statut, Categorie categorie) {
        this.id = id;
        this.libelle = libelle;
        this.description = description;
        this.prix = prix;
        this.stock = stock;
        this.image = image;
        this.statut = statut;
        this.categorie = categorie;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public void setPrix(BigDecimal prix) {
        this.prix = prix;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public StatutProduit getStatut() {
        return statut;
    }

    public void setStatut(StatutProduit statut) {
        this.statut = statut;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public boolean isDisponible() {
        return statut == StatutProduit.DISPONIBLE;
    }

    public boolean isEnRupture() {
        return stock == 0;
    }

    public boolean isStockFaible(int seuil) {
        return stock > 0 && stock <= seuil;
    }

    @Override
    public String toString() {
        return String.format("#%d %-25s %8s F  stock:%-4d %s", id, libelle, prix, stock, statut.getValeurBdd());
    }
}
