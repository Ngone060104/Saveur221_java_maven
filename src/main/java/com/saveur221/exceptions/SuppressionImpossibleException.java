package com.saveur221.exceptions;

/** Levée quand une suppression viole une règle métier (ex : catégorie liée à des produits, règle 9). */
public class SuppressionImpossibleException extends MetierException {
    public SuppressionImpossibleException(String message) {
        super(message);
    }
}
