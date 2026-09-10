package com.saveur221.view;

import java.math.BigDecimal;
import java.util.Scanner;

public final class ConsoleUtils {

    public static final Scanner SCANNER = new Scanner(System.in);

    private ConsoleUtils() {
    }

    public static String lireTexte(String label) {
        System.out.print(label + " : ");
        return SCANNER.nextLine().trim();
    }

    public static String lireTexteObligatoire(String label) {
        String valeur;
        do {
            valeur = lireTexte(label);
            if (valeur.isEmpty()) {
                System.out.println(" Ce champ est obligatoire.");
            }
        } while (valeur.isEmpty());
        return valeur;
    }

      /**
     * NOUVEAUTÉ : Force la saisie d'un texte contenant obligatoirement des lettres.
     * Idéal pour un nom, un prénom, un libellé ou un nom de produit. Rejette les chiffres seuls (ex: "1").
     */

       public static String lireAlphabetiqueObligatoire(String label) {
        while (true) {
            String valeur = lireTexteObligatoire(label);
            // Vérifie si la chaîne contient au moins une lettre minuscule ou majuscule (avec accents)
            if (valeur.matches(".*[a-zA-ZÀ-ÿ].*")) {
                return valeur;
            }
            System.out.println(" Ce champ doit contenir des lettres (pas uniquement des chiffres).");
        }
    }
      /**
     * NOUVEAUTÉ : Force la saisie d'un email au format valide (ex: contact@saveur221.sn).
     */

      public static String lireEmailObligatoire(String label) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        while (true) {
            String valeur = lireTexteObligatoire(label);
            if (valeur.matches(emailRegex)) {
                return valeur;
            }
            System.out.println(" Format d'email invalide (ex: nom@domaine.com).");
        }
    }

    public static int lireEntier(String label) {
        while (true) {
            String saisie = lireTexte(label);
            try {
                return Integer.parseInt(saisie.trim());
            } catch (NumberFormatException e) {
                System.out.println("   Merci de saisir un nombre entier valide.");
            }
        }
    }


     /**
     * NOUVEAUTÉ : Permet de saisir proprement un nombre décimal (double) de manière sécurisée.
     */
    public static double lireDouble(String label) {
        while (true) {
            String saisie = lireTexte(label);
            try {
                return Double.parseDouble(saisie.trim());
            } catch (NumberFormatException e) {
                System.out.println("  ⚠ Merci de saisir un nombre décimal valide (ex: 15.5).");
            }
        }
    }

    public static BigDecimal lireMontant(String label) {
        while (true) {
            String saisie = lireTexte(label);
            try {
                return new BigDecimal(saisie.trim());
            } catch (NumberFormatException e) {
                System.out.println("  ⚠ Merci de saisir un montant valide (ex: 3500 ou 3500.50).");
            }
        }
    }

    public static void titre(String texte) {
        String ligne = "=".repeat(Math.max(texte.length() + 4, 40));
        System.out.println();
        System.out.println(ligne);
        System.out.println("  " + texte);
        System.out.println(ligne);
    }

    public static void sousTitre(String texte) {
        System.out.println();
        System.out.println("--- " + texte + " ---");
    }

    public static void erreur(String message) {
        System.out.println(message);
    }

    public static void succes(String message) {
        System.out.println(message);
    }
}