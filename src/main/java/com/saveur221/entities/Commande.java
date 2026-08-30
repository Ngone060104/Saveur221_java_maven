package com.saveur221.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Commande {

    private Integer id;
    private LocalDateTime dateCommande;
    private BigDecimal montantTotal;
    private StatutCommande statut;
    private Client client;
    private List<LigneCommande> lignes = new ArrayList<>();

    public Commande() {
    }

    public Commande(Integer id, LocalDateTime dateCommande, BigDecimal montantTotal,
                     StatutCommande statut, Client client) {
        this.id = id;
        this.dateCommande = dateCommande;
        this.montantTotal = montantTotal;
        this.statut = statut;
        this.client = client;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(LocalDateTime dateCommande) {
        this.dateCommande = dateCommande;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(BigDecimal montantTotal) {
        this.montantTotal = montantTotal;
    }

    public StatutCommande getStatut() {
        return statut;
    }

    public void setStatut(StatutCommande statut) {
        this.statut = statut;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public List<LigneCommande> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneCommande> lignes) {
        this.lignes = lignes;
    }

    public void ajouterLigne(LigneCommande ligne) {
        this.lignes.add(ligne);
    }

    @Override
    public String toString() {
        return String.format("#%d %s  %8s F  %s  client:%s",
                id, dateCommande, montantTotal, statut, client != null ? client.getNomComplet() : "?");
    }
}
