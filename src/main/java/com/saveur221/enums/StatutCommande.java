package com.saveur221.enums;

/**
 * Statuts possibles d'une commande (correspond au type PostgreSQL
 * statut_commande_enum du script.sql).
 */
public enum StatutCommande {
    EN_ATTENTE,
    EN_PREPARATION,
    PRETE,
    RETIREE,
    ANNULEE
}