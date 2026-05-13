package com.unz.bibliotheque.service;

import org.springframework.stereotype.Component;

/**
 * DESIGN PATTERN : STRATEGY (implémentation concrète)
 * Calcul journalier : 10 FCFA par jour de retard.
 */
@Component
public class PenaliteJournaliereStrategy implements PenaliteStrategy {

    private static final double TARIF_JOURNALIER = 10.0; // 10 FCFA/jour

    @Override
    public double calculerPenalite(long joursRetard) {
        return joursRetard * TARIF_JOURNALIER;
    }
}
