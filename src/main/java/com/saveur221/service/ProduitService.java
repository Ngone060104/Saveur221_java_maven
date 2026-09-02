package com.saveur221.service;

import com.saveur221.entities.Categorie;
import com.saveur221.entities.Produit;
import com.saveur221.enums.StatutProduit;
import com.saveur221.exceptions.RessourceIntrouvableException;
import com.saveur221.exceptions.ValidationException;
import com.saveur221.repository.CategorieRepository;
import com.saveur221.repository.ProduitRepository;

import java.math.BigDecimal;
import java.util.List;

public class ProduitService {

    private final ProduitRepository produitRepository;
    private final CategorieRepository categorieRepository;

    public ProduitService(ProduitRepository produitRepository, CategorieRepository categorieRepository) {
        this.produitRepository = produitRepository;
        this.categorieRepository = categorieRepository;
    }

    public ProduitService() {
        this(new ProduitRepository(), new CategorieRepository());
    }

    public List<Produit> lister() {
        return produitRepository.lister();
    }

    public List<Produit> rechercher(String motCle) {
        return produitRepository.rechercherParLibelle(motCle);
    }

    public List<Produit> filtrerParCategorie(int categorieId) {
        return produitRepository.filtrerParCategorie(categorieId);
    }

    public List<Produit> filtrerParDisponibilite(boolean disponible) {
        return produitRepository.filtrerParDisponibilite(disponible);
    }

    public Produit trouverParId(int id) {
        return produitRepository.trouverParId(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Aucun produit avec l'id #" + id));
    }

    public Produit ajouter(String nom, String description, BigDecimal prix, int stock,
                            String image, int categorieId) {
        validerChampsCommuns(nom, prix, stock);
        if (produitRepository.existeParNom(nom)) {
            throw new ValidationException("Un produit nommé \"" + nom + "\" existe déjà.");
        }
        Categorie categorie = trouverCategorie(categorieId);
        StatutProduit statut = stock > 0 ? StatutProduit.DISPONIBLE : StatutProduit.EN_RUPTURE;
        return produitRepository.creer(new Produit(null, nom.trim(), description, prix, stock, image, statut, categorie));
    }

    public void modifier(int id, String nom, String description, BigDecimal prix, String image, int categorieId) {
        validerChampsCommuns(nom, prix, 0);
        Produit existant = trouverParId(id);
        if (!existant.getNom().equalsIgnoreCase(nom) && produitRepository.existeParNom(nom)) {
            throw new ValidationException("Un produit nommé \"" + nom + "\" existe déjà.");
        }
        Categorie categorie = trouverCategorie(categorieId);
        existant.setNom(nom.trim());
        existant.setDescription(description);
        existant.setPrix(prix);
        existant.setImage(image);
        existant.setCategorie(categorie);
        // Le stock ne se modifie pas ici : c'est le rôle du futur StockService.
        produitRepository.mettreAJour(existant);
    }

    public void supprimer(int id) {
        trouverParId(id);
        produitRepository.supprimer(id);
    }

    private Categorie trouverCategorie(int categorieId) {
        return categorieRepository.trouverParId(categorieId)
                .orElseThrow(() -> new ValidationException("Aucune catégorie avec l'id #" + categorieId));
    }

    private void validerChampsCommuns(String libelle, BigDecimal prix, int stock) {
        if (libelle == null || libelle.isBlank()) throw new ValidationException("Le nom du produit est obligatoire.");
        if (prix == null || prix.signum() < 0) throw new ValidationException("Le prix doit être positif.");
        if (stock < 0) throw new ValidationException("Le stock ne peut pas être négatif.");
    }
}