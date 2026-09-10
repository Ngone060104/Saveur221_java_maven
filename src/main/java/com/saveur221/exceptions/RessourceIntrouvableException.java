package com.saveur221.exceptions;

/** Levée quand une entité recherchée par id n'existe pas en base. */
public class RessourceIntrouvableException extends MetierException {
    public RessourceIntrouvableException(String message) {
        super(message);
    }
}
