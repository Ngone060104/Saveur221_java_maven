package com.saveur221;

import com.saveur221.config.Session;
import com.saveur221.entities.Categorie;
import com.saveur221.entities.Commande;
import com.saveur221.entities.Paiement;
import com.saveur221.entities.Produit;
import com.saveur221.entities.Utilisateur;
import com.saveur221.enums.StatutCommande;
import com.saveur221.exceptions.AuthentificationException;
import com.saveur221.exceptions.MetierException;
import com.saveur221.repository.PaiementRepository.StatutPaiementInfo;
import com.saveur221.service.AuthService;
import com.saveur221.service.CategorieService;
import com.saveur221.service.CommandeService;
import com.saveur221.service.PaiementService;
import com.saveur221.service.ProduitService;
import com.saveur221.service.StatistiqueService;
import com.saveur221.service.StockService;
import com.saveur221.service.UtilisateurService;
import com.saveur221.view.CategorieView;
import com.saveur221.view.CommandeView;
import com.saveur221.view.ConsoleUtils;
import com.saveur221.view.LoginView;
import com.saveur221.view.PaiementView;
import com.saveur221.view.ProduitView;
import com.saveur221.view.StatistiqueView;
import com.saveur221.view.StockView;
import com.saveur221.view.UtilisateurView;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

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
            if (email.equalsIgnoreCase("q"))
                return null;
            String motDePasse = loginView.lireMotDePasse();
            if (motDePasse.equalsIgnoreCase("q"))
                return null;

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
        CommandeService commandeService = new CommandeService();
        CommandeView commandeView = new CommandeView();
        PaiementService paiementService = new PaiementService();
        PaiementView paiementView = new PaiementView();
        StatistiqueService statistiqueService = new StatistiqueService();
        StatistiqueView statistiqueView = new StatistiqueView();
        UtilisateurService utilisateurService = new UtilisateurService();
        UtilisateurView utilisateurView = new UtilisateurView();

        boolean quitter = false;
        while (!quitter) {
            ConsoleUtils.titre("Menu principal — " + Session.getUtilisateurConnecte().getNomComplet()
                    + " (" + Session.getUtilisateurConnecte().getRole() + ")");
            System.out.println("1. Gérer les catégories");
            System.out.println("2. Gérer les produits");
            System.out.println("3. Gérer le stock");
            System.out.println("4. Gérer les commandes");
            System.out.println("5. Gérer les paiements");
            System.out.println("6. Consulter les statistiques");
            if (Session.estAdmin()) {
                System.out.println("7. Gérer les utilisateurs internes (ADMIN)");
            }
            System.out.println("0. Déconnexion");
            String choix = ConsoleUtils.lireTexteObligatoire("Votre choix");

            switch (choix) {
                case "1" -> gererCategories(categorieService, categorieView);
                case "2" -> gererProduits(produitService, categorieService, produitView);
                case "3" -> gererStock(stockService, stockView);
                case "4" -> gererCommandes(commandeService, commandeView);
                case "5" -> gererPaiements(paiementService, paiementView);
                case "6" -> afficherStatistiques(statistiqueService, statistiqueView);
                case "7" -> {
                    if (Session.estAdmin()) {
                        gererUtilisateurs(utilisateurService, utilisateurView);
                    } else {
                        ConsoleUtils.erreur("Accès réservé à l'administrateur.");
                    }
                }
                case "0" -> {
                    Session.deconnecter();
                    ConsoleUtils.succes("Déconnexion réussie.");
                    quitter = true;
                }
                default -> {
                    ConsoleUtils.erreur("Choix invalide.");
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
                        view.afficherSucces(
                                "Catégorie \"" + creee.getLibelle() + "\" créée avec l'id #" + creee.getId());
                    }
                    case "4" -> {
                        int id = view.lireId("Id de la catégorie à modifier");
                        Categorie existante = service.trouverParId(id);
                        
                        // SÉCURISATION : Évite le crash si la catégorie n'existe pas
                        if (existante == null) {
                            view.afficherErreur("Aucune catégorie trouvée avec l'ID #" + id);
                            break;
                        }
                        
                        Categorie maj = view.saisirModificationCategorie(existante);
                        service.modifier(id, maj.getLibelle(), maj.getDescription());
                        view.afficherSucces("Catégorie #" + id + " modifiée.");
                    }
                    case "5" -> {
                        int id = view.lireId("Id de la catégorie à supprimer");
                        Categorie existante = service.trouverParId(id);
                        
                        // SÉCURISATION : Évite le crash si la catégorie n'existe pas
                        if (existante == null) {
                            view.afficherErreur("Aucune catégorie trouvée avec l'ID #" + id);
                            break;
                        }
                        
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
                        
                        // SÉCURISATION : Vérifie si la catégorie ciblée existe avant de filtrer
                        if (categorieService.trouverParId(categorieId) == null) {
                            view.afficherErreur("Aucune catégorie trouvée avec l'ID #" + categorieId);
                            break;
                        }
                        view.afficherListe(service.filtrerParCategorie(categorieId));
                    }
                    case "4" -> view.afficherListe(service.filtrerParDisponibilite(view.demanderDisponibles()));
                    case "5" -> {
                        var categories = categorieService.lister();
                        if (categories.isEmpty()) {
                            view.afficherErreur("Impossible d'ajouter un produit car il n'existe aucune catégorie.");
                            break;
                        }
                        
                        var saisie = view.saisirNouveauProduit(categories);
                        // SÉCURISATION : Vérifie si la catégorie choisie par l'utilisateur existe
                        if (categorieService.trouverParId(saisie.categorieId()) == null) {
                            view.afficherErreur("La catégorie #" + saisie.categorieId() + " n'existe pas.");
                            break;
                        }
                        
                        Produit cree = service.ajouter(saisie.nom(), saisie.description(), saisie.prix(),
                                saisie.stock(), saisie.image(), saisie.categorieId());
                        view.afficherSucces("Produit \"" + cree.getNom() + "\" créé avec l'id #" + cree.getId());
                    }
                    case "6" -> {
                        int id = view.lireId("Id du produit à modifier");
                        Produit existant = service.trouverParId(id);
                        
                        // SÉCURISATION : Évite le crash NullPointerException
                        if (existant == null) {
                            view.afficherErreur("Aucun produit trouvé avec l'ID #" + id);
                            break;
                        }
                        
                        var saisie = view.saisirModificationProduit(existant, categorieService.lister());
                        // SÉCURISATION : Vérifie si la nouvelle catégorie choisie existe
                        if (categorieService.trouverParId(saisie.categorieId()) == null) {
                            view.afficherErreur("La catégorie #" + saisie.categorieId() + " n'existe pas.");
                            break;
                        }
                        
                        service.modifier(id, saisie.nom(), saisie.description(), saisie.prix(),
                                saisie.image(), saisie.categorieId());
                        view.afficherSucces("Produit #" + id + " modifié.");
                    }
                    case "7" -> {
                        int id = view.lireId("Id du produit à supprimer");
                        Produit existant = service.trouverParId(id);
                        
                        // SÉCURISATION : Évite le crash NullPointerException
                        if (existant == null) {
                            view.afficherErreur("Aucun produit trouvé avec l'ID #" + id);
                            break;
                        }
                        
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
            } catch (MetierException e) {
                view.afficherErreur(e.getMessage());
            }
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
                        
                        // SÉCURISATION : On délègue l'approvisionnement et on s'assure du retour
                        Produit maj = service.approvisionner(id, quantite);
                        
                        // Si le service ne lève pas d'exception mais renvoie null si inconnu
                        if (maj == null) {
                            view.afficherErreur("Aucun produit trouvé avec l'ID #" + id);
                            break;
                        }
                        
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
        }
    }

    private static void gererCommandes(CommandeService service, CommandeView view) {
        boolean retour = false;
        while (!retour) {
            String choix = view.afficherMenu();
            try {
                switch (choix) {
                    case "1" -> view.afficherListe(service.lister());
                    case "2" -> {
                        StatutCommande statut = view.choisirStatut();
                        if (statut != null)
                            view.afficherListe(service.filtrerParStatut(statut));
                        else
                            view.afficherErreur("Statut invalide.");
                    }
                    case "3" -> view.afficherListe(service.listerEnCours());
                    case "4" -> {
                        int id = view.lireId("Id de la commande");
                        Commande commande = service.trouverParId(id);
                        
                        // SÉCURISATION : Évite le plantage si la commande n'existe pas
                        if (commande == null) {
                            view.afficherErreur("Aucune commande trouvée avec l'ID #" + id);
                            break;
                        }
                        view.afficherDetail(commande);
                    }
                    case "5" -> {
                        int id = view.lireId("Id de la commande");
                        Commande commande = service.trouverParId(id);
                        
                        // SÉCURISATION : Évite le plantage si la commande n'existe pas
                        if (commande == null) {
                            view.afficherErreur("Aucune commande trouvée avec l'ID #" + id);
                            break;
                        }
                        
                        view.afficherStatutActuel(commande.getStatut());
                        StatutCommande nouveauStatut = view.choisirStatut();
                        if (nouveauStatut != null) {
                            service.changerStatut(id, nouveauStatut);
                            view.afficherSucces("Commande #" + id + " -> " + nouveauStatut);
                        } else {
                            view.afficherErreur("Statut invalide.");
                        }
                    }
                    case "6" -> {
                        int id = view.lireId("Id de la commande à annuler");
                        Commande commande = service.trouverParId(id);
                        
                        // SÉCURISATION : Évite le plantage si la commande n'existe pas
                        if (commande == null) {
                            view.afficherErreur("Aucune commande trouvée avec l'ID #" + id);
                            break;
                        }
                        
                        if (view.demanderConfirmationAnnulation(id, commande.getStatut())) {
                            service.annuler(id);
                            view.afficherSucces("Commande annulée. Le stock a été restitué automatiquement.");
                        } else {
                            view.afficherMessage("Annulation abandonnée.");
                        }
                    }
                    case "0" -> retour = true;
                    default -> view.afficherErreur("Choix invalide.");
                }
            } catch (MetierException e) {
                view.afficherErreur(e.getMessage());
            }
        }
    }


    private static void gererPaiements(PaiementService service, PaiementView view) {
        boolean retour = false;
        while (!retour) {
            String choix = view.afficherMenu();
            try {
                switch (choix) {
                    case "1" -> {
                        int commandeId = view.lireIdCommande();
                        
                        // SÉCURISATION : Vérifie si les informations de paiement de la commande existent
                        StatutPaiementInfo info = service.getStatutPaiement(commandeId);
                        if (info == null) {
                            view.afficherErreur("Aucune commande trouvée avec l'ID #" + commandeId);
                            break;
                        }
                        
                        var paiements = service.listerParCommande(commandeId);
                        view.afficherPaiementsEtStatut(paiements, info);
                    }
                    case "2" -> view.afficherImpayeesEtPartielles(service.listerCommandesImpayeesOuPartielles());
                    case "3" -> {
                        int commandeId = view.lireIdCommande();
                        
                        // SÉCURISATION : Vérifie si les informations de paiement de la commande existent
                        StatutPaiementInfo info = service.getStatutPaiement(commandeId);
                        if (info == null) {
                            view.afficherErreur("Aucune commande trouvée avec l'ID #" + commandeId);
                            break;
                        }
                        
                        view.afficherMontantRestant(info.montantRestant);
                        if (view.estDejaSoldee(info.montantRestant)) {
                            view.afficherMessage("Cette commande est déjà totalement payée.");
                        } else {
                            BigDecimal montant = view.lireMontantRecu();
                            service.enregistrer(commandeId, montant);
                            view.afficherSucces(
                                    "Paiement de " + montant + " F enregistré pour la commande #" + commandeId + ".");
                        }
                    }
                    case "0" -> retour = true;
                    default -> view.afficherErreur("Choix invalide.");
                }
            } catch (MetierException e) {
                view.afficherErreur(e.getMessage());
            }
        } 
    } 


    private static void afficherStatistiques(StatistiqueService service, StatistiqueView view) {
        Map<StatutCommande, Integer> commandesParStatut = new LinkedHashMap<>();
        
        try {
            // OPTIMISATION : On charge les compteurs par statut
            for (StatutCommande s : StatutCommande.values()) {
                int nb = service.compterParStatut(s);
                commandesParStatut.put(s, nb);
            }

            // Affichage sécurisé de l'ensemble des indicateurs de performance
            view.afficherStatistiques(
                    service.chiffreAffairesDuJour(),
                    service.chiffreAffairesDeLaSemaine(),
                    service.chiffreAffairesDuMois(),
                    service.nombreDeCommandes(),
                    service.commandesEnCours(),
                    commandesParStatut,
                    service.produitLePlusVendu(),
                    service.topProduits(3)
            );
        } catch (Exception e) {
            // Sécurité si un calcul de CA ou de top produit échoue en base (Données vides)
            view.afficherErreur("Impossible de charger le tableau de bord complet : " + e.getMessage());
        }
    }


    
    private static void gererUtilisateurs(UtilisateurService service, UtilisateurView view) {
        boolean retour = false;
        while (!retour) {
            String choix = view.afficherMenu();
            try {
                switch (choix) {
                    case "1" -> view.afficherListe(service.lister());
                    case "2" -> view.afficherListe(service.rechercher(view.lireMotCleRecherche()));
                    case "3" -> {
                        var saisie = view.saisirNouvelUtilisateur();
                        Utilisateur cree = service.ajouter(saisie.nom(), saisie.prenom(), saisie.email(),
                                saisie.motDePasse(), saisie.role());
                        view.afficherSucces("Utilisateur \"" + cree.getNomComplet() + "\" créé avec l'id #" + cree.getId());
                    }
                    case "4" -> {
                        int id = view.lireId("Id de l'utilisateur à modifier");
                        Utilisateur existant = service.trouverParId(id);
                        
                        // SÉCURISATION : Évite le plantage si l'utilisateur n'existe pas
                        if (existant == null) {
                            view.afficherErreur("Aucun utilisateur trouvé avec l'ID #" + id);
                            break;
                        }
                        
                        var saisie = view.saisirModificationUtilisateur(existant);
                        service.modifier(id, saisie.nom(), saisie.prenom(), saisie.email(), saisie.role());
                        view.afficherSucces("Utilisateur #" + id + " modifié.");
                    }
                    case "5" -> {
                        int id = view.lireId("Id de l'utilisateur");
                        Utilisateur existant = service.trouverParId(id);
                        
                        // SÉCURISATION : Évite le plantage si l'utilisateur n'existe pas
                        if (existant == null) {
                            view.afficherErreur("Aucun utilisateur trouvé avec l'ID #" + id);
                            break;
                        }
                        
                        boolean nouveauStatut = !existant.isActif();
                        String action = nouveauStatut ? "activer" : "désactiver";
                        if (view.demanderConfirmation("Confirmer : " + action + " le compte de " + existant.getNomComplet() + " ?")) {
                            service.changerActif(id, nouveauStatut);
                            view.afficherSucces("Compte " + (nouveauStatut ? "activé." : "désactivé."));
                        } else {
                            view.afficherMessage("Action annulée.");
                        }
                    }
                    case "6" -> {
                        int id = view.lireId("Id de l'utilisateur à supprimer");
                        Utilisateur existant = service.trouverParId(id);
                        
                        // SÉCURISATION 1 : Évite le plantage si l'utilisateur n'existe pas
                        if (existant == null) {
                            view.afficherErreur("Aucun utilisateur trouvé avec l'ID #" + id);
                            break;
                        }
                        
                        // SÉCURISATION 2 : Empêche l'auto-suppression après vérification de l'existence
                        if (Session.getUtilisateurConnecte().getId().equals(id)) {
                            view.afficherErreur("Vous ne pouvez pas supprimer votre propre compte.");
                        } else {
                            if (view.demanderConfirmation("Confirmer la suppression de \"" + existant.getNomComplet() + "\" ?")) {
                                service.supprimer(id);
                                view.afficherSucces("Utilisateur supprimé.");
                            } else {
                                view.afficherMessage("Suppression annulée.");
                            }
                        }
                    }
                    case "0" -> retour = true;
                    default -> view.afficherErreur("Choix invalide.");
                }
            } catch (MetierException e) {
                view.afficherErreur(e.getMessage());
            }
        }
    }

}
