package com.saveur221.service;

import com.saveur221.entities.Commande;
import com.saveur221.enums.StatutCommande;
import com.saveur221.exceptions.RessourceIntrouvableException;
import com.saveur221.exceptions.ValidationException;
import com.saveur221.repository.CommandeRepository;
import com.saveur221.repository.LigneCommandeRepository;

import java.util.List;

public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final LigneCommandeRepository ligneCommandeRepository;

    // Ordre normal des statuts, pour empêcher les sauts d'étape.
    private static final List<StatutCommande> ORDRE_NORMAL = List.of(
            StatutCommande.EN_ATTENTE, StatutCommande.EN_PREPARATION,
            StatutCommande.PRETE, StatutCommande.RETIREE);

    public CommandeService(CommandeRepository commandeRepository, LigneCommandeRepository ligneCommandeRepository) {
        this.commandeRepository = commandeRepository;
        this.ligneCommandeRepository = ligneCommandeRepository;
    }

    public CommandeService() {
        this(new CommandeRepository(), new LigneCommandeRepository());
    }

    public List<Commande> lister() {
        return commandeRepository.lister();
    }

    public List<Commande> filtrerParStatut(StatutCommande statut) {
        return commandeRepository.filtrerParStatut(statut);
    }

    public List<Commande> listerEnCours() {
        return commandeRepository.listerEnCours();
    }

    public Commande trouverParId(int id) {
        Commande commande = commandeRepository.trouverParId(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Aucune commande avec l'id #" + id));
        commande.setLignes(ligneCommandeRepository.listerParCommande(id));
        return commande;
    }

    /**
     * Fait avancer une commande au statut suivant dans le cycle normal.
     * Une commande RETIREE ou ANNULEE est définitive.
     */
    public void changerStatut(int id, StatutCommande nouveauStatut) {
        Commande commande = trouverParId(id);
        StatutCommande actuel = commande.getStatut();

        if (actuel == StatutCommande.RETIREE || actuel == StatutCommande.ANNULEE) {
            throw new ValidationException(
                "Cette commande est " + actuel + " : son statut ne peut plus être modifié.");
        }
        if (nouveauStatut == StatutCommande.ANNULEE) {
            throw new ValidationException("Utilisez l'action \"Annuler\" pour annuler une commande.");
        }
        int indexActuel = ORDRE_NORMAL.indexOf(actuel);
        int indexNouveau = ORDRE_NORMAL.indexOf(nouveauStatut);
        if (indexNouveau != indexActuel + 1) {
            throw new ValidationException(
                "Transition invalide : une commande " + actuel + " ne peut passer qu'à "
                + ORDRE_NORMAL.get(indexActuel + 1) + ".");
        }
        commandeRepository.changerStatut(id, nouveauStatut);
    }

    /**
     * Annule une commande. La restitution du stock est assurée par le trigger
     * PostgreSQL trg_commande_annulation (partagé avec le module PHP).
     */
    public void annuler(int id) {
        Commande commande = trouverParId(id);
        if (commande.getStatut() == StatutCommande.RETIREE) {
            throw new ValidationException("Une commande déjà retirée ne peut plus être annulée.");
        }
        if (commande.getStatut() == StatutCommande.ANNULEE) {
            throw new ValidationException("Cette commande est déjà annulée.");
        }
        commandeRepository.changerStatut(id, StatutCommande.ANNULEE);
    }
}