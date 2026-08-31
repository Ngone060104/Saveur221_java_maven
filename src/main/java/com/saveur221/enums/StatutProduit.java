package com.saveur221.enums;

/**
 * Statut de disponibilité d'un produit (correspond au type PostgreSQL
 * statut_produit_enum du script.sql). Synchronisé automatiquement avec
 * le stock par un trigger côté base, et recalculé côté service par sécurité.
 */
public enum StatutProduit {
    DISPONIBLE("disponible"),
    EN_RUPTURE("en_rupture");

    private final String valeurBdd;

    StatutProduit(String valeurBdd) {
        this.valeurBdd = valeurBdd;
    }

    public String getValeurBdd() {
        return valeurBdd;
    }

    public static StatutProduit fromValeurBdd(String valeur) {
        for (StatutProduit s : values()) {
            if (s.valeurBdd.equals(valeur)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Statut produit inconnu : " + valeur);
    }
}
