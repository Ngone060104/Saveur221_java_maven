package com.saveur221.view;

import com.saveur221.enums.StatutCommande;
import com.saveur221.repository.LigneCommandeRepository.ProduitVendu;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.saveur221.view.ConsoleUtils.*;

/** Vue pure : affichage uniquement. Ne connaît pas StatistiqueService. */
public class StatistiqueView {

    public void afficherStatistiques(BigDecimal caJour, BigDecimal caSemaine, BigDecimal caMois,
                                      int nombreCommandes, int commandesEnCours,
                                      Map<StatutCommande, Integer> commandesParStatut,
                                      ProduitVendu produitLePlusVendu, List<ProduitVendu> topProduits) {
        titre("Statistiques");

        System.out.println("Chiffre d'affaires du jour      : " + caJour + " F");
        System.out.println("Chiffre d'affaires de la semaine : " + caSemaine + " F");
        System.out.println("Chiffre d'affaires du mois       : " + caMois + " F");
        System.out.println();
        System.out.println("Nombre total de commandes        : " + nombreCommandes);
        System.out.println("Commandes en cours                : " + commandesEnCours);
        System.out.println();
        System.out.println("Commandes par statut :");
        for (Map.Entry<StatutCommande, Integer> entry : commandesParStatut.entrySet()) {
            System.out.printf("  %-15s : %d%n", entry.getKey(), entry.getValue());
        }

        System.out.println();
        if (produitLePlusVendu != null) {
            System.out.printf("Produit le plus vendu : %s (%d ventes)%n",
                    produitLePlusVendu.nom, produitLePlusVendu.quantiteVendue);
        } else {
            System.out.println("Produit le plus vendu : aucune vente enregistrée.");
        }

        System.out.println();
        System.out.println("Top 3 des produits :");
        if (topProduits.isEmpty()) {
            System.out.println("  Aucune vente enregistrée.");
        } else {
            int rang = 1;
            for (ProduitVendu p : topProduits) {
                System.out.printf("  %d. %-25s %d vente(s)%n", rang++, p.nom, p.quantiteVendue);
            }
        }
    }

    public void pause() {
        ConsoleUtils.pause();
    }
}