package com.saveur221.exceptions;

/** Levée quand une donnée saisie ne respecte pas une contrainte de validation (email unique, mdp trop court...). */
public class ValidationException extends MetierException {
    public ValidationException(String message) {
        super(message);
    }
}
