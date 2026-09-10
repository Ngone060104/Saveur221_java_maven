package com.saveur221.service;

import com.saveur221.enums.StatutCommande;
import com.saveur221.repository.CommandeRepository;
import com.saveur221.repository.LigneCommandeRepository;
import com.saveur221.repository.LigneCommandeRepository.ProduitVendu;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

public class StatistiqueService {

    private final CommandeRepository commandeRepository;
    private final LigneCommandeRepository ligneCommandeRepository;

    public StatistiqueService(CommandeRepository commandeRepository, LigneCommandeRepository ligneCommandeRepository) {
        this.commandeRepository = commandeRepository;
        this.ligneCommandeRepository = ligneCommandeRepository;
    }

    public StatistiqueService() {
        this(new CommandeRepository(), new LigneCommandeRepository());
    }

    public BigDecimal chiffreAffairesDuJour() {
        return commandeRepository.chiffreAffaires(LocalDate.now().atStartOfDay());
    }

    public BigDecimal chiffreAffairesDeLaSemaine() {
        LocalDateTime debutSemaine = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();
        return commandeRepository.chiffreAffaires(debutSemaine);
    }

    public BigDecimal chiffreAffairesDuMois() {
        return commandeRepository.chiffreAffaires(LocalDate.now().withDayOfMonth(1).atStartOfDay());
    }

    public int nombreDeCommandes() {
        return commandeRepository.compterCommandes();
    }

    public int commandesEnCours() {
        return commandeRepository.compterCommandesEnCours();
    }

    public int compterParStatut(StatutCommande statut) {
        return commandeRepository.compterParStatut(statut);
    }

    public List<ProduitVendu> topProduits(int limite) {
        return ligneCommandeRepository.produitsLesPlusVendus(limite);
    }

    /** @return le produit le plus vendu, ou null si aucune vente enregistrée. */
    public ProduitVendu produitLePlusVendu() {
        List<ProduitVendu> top = topProduits(1);
        return top.isEmpty() ? null : top.get(0);
    }
}