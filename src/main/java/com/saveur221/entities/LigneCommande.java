package com.saveur221.entities;

import java.math.BigDecimal;

public class LigneCommande {

    private Integer id;
    private int quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal montantLigne;
    private Integer commandeId;
    private Produit produit;

    public LigneCommande() {
    }

    public LigneCommande(Integer id, int quantite, BigDecimal prixUnitaire,
                          BigDecimal montantLigne, Integer commandeId, Produit produit) {
        this.id = id;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.montantLigne = montantLigne;
        this.commandeId = commandeId;
        this.produit = produit;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public BigDecimal getMontantLigne() {
        return montantLigne;
    }

    public void setMontantLigne(BigDecimal montantLigne) {
        this.montantLigne = montantLigne;
    }

    public Integer getCommandeId() {
        return commandeId;
    }

    public void setCommandeId(Integer commandeId) {
        this.commandeId = commandeId;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    @Override
    public String toString() {
        return String.format("%2d x %-25s = %8s F", quantite, produit.getLibelle(), montantLigne);
    }
}
