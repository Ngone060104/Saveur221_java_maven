package com.saveur221.view;

import java.util.Scanner;

public final class ConsoleUtils {

    public static final Scanner SCANNER = new Scanner(System.in);

    private ConsoleUtils() {
    }

    public static String lireTexte(String label) {
        System.out.print(label + " : ");
        return SCANNER.nextLine().trim();
    }

    public static void titre(String texte) {
        String ligne = "=".repeat(Math.max(texte.length() + 4, 40));
        System.out.println();
        System.out.println(ligne);
        System.out.println("  " + texte);
        System.out.println(ligne);
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