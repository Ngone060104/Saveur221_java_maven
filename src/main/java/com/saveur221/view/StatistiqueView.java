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
        titre("Tableau de bord - Statistiques");

        // SECTION CHIFFRE D'AFFAIRES
        sousTitre("Chiffre d'Affaires");
        System.out.printf("  • Aujourd'hui : %s F%n", caJour);
        System.out.printf("  • Cette semaine : %s F%n", caSemaine);
        System.out.printf("  • Ce mois-ci    : %s F%n", caMois);

        // SECTION COMMANDES
        sousTitre("Activité des Commandes");
        System.out.printf("  • Nombre total de commandes : %d%n", nombreCommandes);
        System.out.printf("  • Commandes en cours (actives) : %d%n", commandesEnCours);
        System.out.println("\n  Détail par statut :");
        if (commandesParStatut == null || commandesParStatut.isEmpty()) {
            System.out.println("    Aucune donnée disponible.");
        } else {
            for (Map.Entry<StatutCommande, Integer> entry : commandesParStatut.entrySet()) {
                System.out.printf("    %-15s : %d%n", entry.getKey(), entry.getValue());
            }
        }

        // ECTION VENTES ET PALMARÈS
        sousTitre("Performances des Produits");
        if (produitLePlusVendu != null) {
            System.out.printf("  ⭐ Produit star : %s (%d ventes)%n",
                    produitLePlusVendu.nom, produitLePlusVendu.quantiteVendue);
        } else {
            System.out.println(" Produit star : Aucune vente enregistrée.");
        }

        System.out.println("\n  Top 3 des produits les plus vendus :");
        if (topProduits == null || topProduits.isEmpty()) {
            System.out.println(" Aucune vente enregistrée.");
        } else {
            int rang = 1;
            for (ProduitVendu p : topProduits) {
                System.out.printf("    %d. %-25s (%d vente(s))%n", rang++, p.nom, p.quantiteVendue);
            }
        }
        System.out.println();
    }

        public void afficherErreur(String message) { 
        erreur(message); 
    }

    public void afficherSucces(String message) { 
        succes(message); 
    }

}
