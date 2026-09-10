package com.saveur221.service;

import com.saveur221.entities.Categorie;
import com.saveur221.exceptions.RessourceIntrouvableException;
import com.saveur221.exceptions.SuppressionImpossibleException;
import com.saveur221.exceptions.ValidationException;
import com.saveur221.repository.CategorieRepository;

import java.util.List;

public class CategorieService {

    private final CategorieRepository categorieRepository;

    public CategorieService(CategorieRepository categorieRepository) {
        this.categorieRepository = categorieRepository;
    }

    public CategorieService() {
        this(new CategorieRepository());
    }

    public List<Categorie> lister() {
        return categorieRepository.lister();
    }

    public List<Categorie> rechercher(String motCle) {
        return categorieRepository.rechercherParNom(motCle);
    }

    public Categorie trouverParId(int id) {
        return categorieRepository.trouverParId(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Aucune catégorie avec l'id #" + id));
    }

    public Categorie ajouter(String libelle, String description) {
        valider(libelle);
        if (categorieRepository.existeParLibelle(libelle)) {
            throw new ValidationException("Une catégorie nommée \"" + libelle + "\" existe déjà.");
        }
        return categorieRepository.creer(new Categorie(null, libelle.trim(), description));
    }

    public void modifier(int id, String libelle, String description) {
        valider(libelle);
        Categorie existante = trouverParId(id);
        if (!existante.getLibelle().equalsIgnoreCase(libelle) && categorieRepository.existeParLibelle(libelle)) {
            throw new ValidationException("Une catégorie nommée \"" + libelle + "\" existe déjà.");
        }
        existante.setLibelle(libelle.trim());
        existante.setDescription(description);
        categorieRepository.mettreAJour(existante);
    }

    /** Règle métier : une catégorie contenant des produits ne peut pas être supprimée. */
    public void supprimer(int id) {
        trouverParId(id);
        int nbProduits = categorieRepository.compterProduitsLies(id);
        if (nbProduits > 0) {
            throw new SuppressionImpossibleException(
                "Impossible de supprimer : " + nbProduits + " produit(s) sont rattachés à cette catégorie.");
        }
        categorieRepository.supprimer(id);
    }

    private void valider(String libelle) {
        if (libelle == null || libelle.isBlank()) {
            throw new ValidationException("Le libellé de la catégorie est obligatoire.");
        }
    }
}