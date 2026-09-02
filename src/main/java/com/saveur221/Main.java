package com.saveur221;

import com.saveur221.config.Session;
import com.saveur221.entities.Categorie;
import com.saveur221.entities.Produit;
import com.saveur221.entities.Utilisateur;
import com.saveur221.exceptions.AuthentificationException;
import com.saveur221.exceptions.MetierException;
import com.saveur221.service.AuthService;
import com.saveur221.service.CategorieService;
import com.saveur221.service.ProduitService;
import com.saveur221.service.StockService;
import com.saveur221.view.CategorieView;
import com.saveur221.view.ConsoleUtils;
import com.saveur221.view.LoginView;
import com.saveur221.view.ProduitView;
import com.saveur221.view.StockView;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        AuthService authService = new AuthService();
        LoginView loginView = new LoginView();

        Utilisateur utilisateur = seConnecter(authService, loginView);
        if (utilisateur == null) {
            System.out.println("\nÀ bientôt !");
            return;
        }

        menuPrincipal();
        System.out.println("\nÀ bientôt !");
    }

    private static Utilisateur seConnecter(AuthService authService, LoginView loginView) {
        loginView.afficherEnTete();
        while (true) {
            String email = loginView.lireEmail();
            if (email.equalsIgnoreCase("q")) return null;
            String motDePasse = loginView.lireMotDePasse();
            if (motDePasse.equalsIgnoreCase("q")) return null;

            try {
                Utilisateur utilisateur = authService.connexion(email, motDePasse);
                Session.connecter(utilisateur);
                loginView.afficherBienvenue(utilisateur);
                return utilisateur;
            } catch (AuthentificationException e) {
                loginView.afficherErreur(e.getMessage());
            }
        }
    }


    private static void menuPrincipal() {
        CategorieService categorieService = new CategorieService();
        CategorieView categorieView = new CategorieView();
        ProduitService produitService = new ProduitService();
        ProduitView produitView = new ProduitView();
        StockService stockService = new StockService();
        StockView stockView = new StockView();
 
        boolean quitter = false;
        while (!quitter) {
            ConsoleUtils.titre("Menu principal — " + Session.getUtilisateurConnecte().getNomComplet()
                    + " (" + Session.getUtilisateurConnecte().getRole() + ")");
            System.out.println("1. Gérer les catégories");
            System.out.println("2. Gérer les produits");
            System.out.println("3. Gérer le stock");
            System.out.println("0. Déconnexion");
            String choix = ConsoleUtils.lireTexte("Votre choix");
 
            switch (choix) {
                case "1" -> gererCategories(categorieService, categorieView);
                case "2" -> gererProduits(produitService, categorieService, produitView);
                case "3" -> gererStock(stockService, stockView);
                case "0" -> {
                    Session.deconnecter();
                    ConsoleUtils.succes("Déconnexion réussie.");
                    quitter = true;
                }
                default -> {
                    ConsoleUtils.erreur("Choix invalide.");
                    ConsoleUtils.pause();
                }
            }
        }
    }

    private static void gererCategories(CategorieService service, CategorieView view) {
        boolean retour = false;
        while (!retour) {
            String choix = view.afficherMenu();
            try {
                switch (choix) {
                    case "1" -> view.afficherListe(service.lister());
                    case "2" -> view.afficherListe(service.rechercher(view.lireMotCleRecherche()));
                    case "3" -> {
                        Categorie saisie = view.saisirNouvelleCategorie();
                        Categorie creee = service.ajouter(saisie.getLibelle(), saisie.getDescription());
                        view.afficherSucces("Catégorie \"" + creee.getLibelle() + "\" créée avec l'id #" + creee.getId());
                    }
                    case "4" -> {
                        int id = view.lireId("Id de la catégorie à modifier");
                        Categorie existante = service.trouverParId(id);
                        Categorie maj = view.saisirModificationCategorie(existante);
                        service.modifier(id, maj.getLibelle(), maj.getDescription());
                        view.afficherSucces("Catégorie #" + id + " modifiée.");
                    }
                    case "5" -> {
                        int id = view.lireId("Id de la catégorie à supprimer");
                        Categorie existante = service.trouverParId(id);
                        if (view.demanderConfirmationSuppression(existante.getLibelle())) {
                            service.supprimer(id);
                            view.afficherSucces("Catégorie supprimée.");
                        } else {
                            view.afficherMessage("Suppression annulée.");
                        }
                    }
                    case "0" -> retour = true;
                    default -> view.afficherErreur("Choix invalide.");
                }
            } catch (MetierException e) {
                view.afficherErreur(e.getMessage());
            }
            if (!retour) {
                view.pause();
            }
        }
    }


    private static void gererProduits(ProduitService service, CategorieService categorieService, ProduitView view) {
    boolean retour = false;
    while (!retour) {
        String choix = view.afficherMenu();
        try {
            switch (choix) {
                case "1" -> view.afficherListe(service.lister());
                case "2" -> view.afficherListe(service.rechercher(view.lireMotCleRecherche()));
                case "3" -> {
                    view.afficherCategoriesDisponibles(categorieService.lister());
                    int categorieId = view.lireId("Id de la catégorie");
                    view.afficherListe(service.filtrerParCategorie(categorieId));
                }
                case "4" -> view.afficherListe(service.filtrerParDisponibilite(view.demanderDisponibles()));
                case "5" -> {
                    var saisie = view.saisirNouveauProduit(categorieService.lister());
                    var cree = service.ajouter(saisie.nom(), saisie.description(), saisie.prix(),
                            saisie.stock(), saisie.image(), saisie.categorieId());
                    view.afficherSucces("Produit \"" + cree.getNom() + "\" créé avec l'id #" + cree.getId());
                }
                case "6" -> {
                    int id = view.lireId("Id du produit à modifier");
                    var existant = service.trouverParId(id);
                    var saisie = view.saisirModificationProduit(existant, categorieService.lister());
                    service.modifier(id, saisie.nom(), saisie.description(), saisie.prix(),
                            saisie.image(), saisie.categorieId());
                    view.afficherSucces("Produit #" + id + " modifié.");
                }
                case "7" -> {
                    int id = view.lireId("Id du produit à supprimer");
                    var existant = service.trouverParId(id);
                    if (view.demanderConfirmationSuppression(existant.getNom())) {
                        service.supprimer(id);
                        view.afficherSucces("Produit supprimé.");
                    } else {
                        view.afficherMessage("Suppression annulée.");
                    }
                }
                case "0" -> retour = true;
                default -> view.afficherErreur("Choix invalide.");
            }
        } catch (com.saveur221.exceptions.MetierException e) {
            view.afficherErreur(e.getMessage());
        }
        if (!retour) view.pause();
    }
}



    private static void gererStock(StockService service, StockView view) {
        boolean retour = false;
        while (!retour) {
            String choix = view.afficherMenu(service.getSeuilAlerte());
            try {
                switch (choix) {
                    case "1" -> view.afficherListe(service.consulterStock());
                    case "2" -> {
                        int id = view.lireIdProduit();
                        int quantite = view.lireQuantiteAAjouter();
                        Produit maj = service.approvisionner(id, quantite);
                        view.afficherSucces("Nouveau stock de \"" + maj.getNom() + "\" : " + maj.getStock()
                                + " (" + maj.getStatut().getValeurBdd() + ")");
                    }
                    case "3" -> {
                        int nouveauSeuil = view.lireNouveauSeuil();
                        service.definirSeuilAlerte(nouveauSeuil);
                        view.afficherSucces("Seuil d'alerte mis à jour : " + nouveauSeuil);
                    }
                    case "4" -> view.afficherListe(service.listerStockFaible());
                    case "5" -> view.afficherListe(service.listerEnRupture());
                    case "0" -> retour = true;
                    default -> view.afficherErreur("Choix invalide.");
                }
            } catch (MetierException e) {
                view.afficherErreur(e.getMessage());
            }
            if (!retour) view.pause();
        }
    }
}
