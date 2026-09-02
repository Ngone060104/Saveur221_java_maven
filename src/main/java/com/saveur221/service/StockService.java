package com.saveur221.service;

import com.saveur221.entities.Produit;
import com.saveur221.enums.StatutProduit;
import com.saveur221.exceptions.RessourceIntrouvableException;
import com.saveur221.exceptions.ValidationException;
import com.saveur221.repository.ProduitRepository;

import java.util.List;

/**
 * Gestion du stock : consultation, approvisionnement, seuil d'alerte,
 * détection des produits en rupture ou en stock faible.
 *
 * Règle métier : si quantite_stock = 0, le produit doit être indisponible.
 */
public class StockService {

    private static final int SEUIL_PAR_DEFAUT = 5;

    private final ProduitRepository produitRepository;
    private int seuilAlerte = SEUIL_PAR_DEFAUT;

    public StockService(ProduitRepository produitRepository) {
        this.produitRepository = produitRepository;
    }

    public StockService() {
        this(new ProduitRepository());
    }

    public int getSeuilAlerte() {
        return seuilAlerte;
    }

    public void definirSeuilAlerte(int seuil) {
        if (seuil < 0) {
            throw new ValidationException("Le seuil d'alerte ne peut pas être négatif.");
        }
        this.seuilAlerte = seuil;
    }

    public List<Produit> consulterStock() {
        return produitRepository.lister();
    }

    public List<Produit> listerStockFaible() {
        return produitRepository.lister().stream()
                .filter(p -> p.getStock() > 0 && p.getStock() <= seuilAlerte)
                .toList();
    }

    public List<Produit> listerEnRupture() {
        return produitRepository.lister().stream()
                .filter(p -> p.getStock() == 0)
                .toList();
    }

    /**
     * Ajoute {@code quantite} au stock existant et remet automatiquement
     * le statut à "disponible" (un réapprovisionnement sort de la rupture).
     */
    public Produit approvisionner(int produitId, int quantite) {
        if (quantite <= 0) {
            throw new ValidationException("La quantité à ajouter doit être positive.");
        }
        Produit produit = trouverParId(produitId);
        produit.setStock(produit.getStock() + quantite);
        produit.setStatut(StatutProduit.DISPONIBLE);
        produitRepository.mettreAJourStock(produitId, produit.getStock(), produit.getStatut());
        return produit;
    }

    private Produit trouverParId(int id) {
        return produitRepository.trouverParId(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Aucun produit avec l'id #" + id));
    }
}