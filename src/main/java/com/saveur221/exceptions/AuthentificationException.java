package com.saveur221.exceptions;

/** Levée quand l'email/mot de passe est invalide ou le compte inactif. */
public class AuthentificationException extends MetierException {
    public AuthentificationException(String message) {
        super(message);
    }
}