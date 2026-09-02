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
                System.out.println("  ⚠ Ce champ est obligatoire.");
            }
        } while (valeur.isEmpty());
        return valeur;
    }

    public static int lireEntier(String label) {
        while (true) {
            String saisie = lireTexte(label);
            try {
                return Integer.parseInt(saisie.trim());
            } catch (NumberFormatException e) {
                System.out.println("  ⚠ Merci de saisir un nombre entier valide.");
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
        System.out.println("  ✗ " + message);
    }

    public static void succes(String message) {
        System.out.println("  ✓ " + message);
    }

    public static void pause() {
        System.out.print("\nAppuyez sur Entrée pour continuer...");
        SCANNER.nextLine();
    }
}