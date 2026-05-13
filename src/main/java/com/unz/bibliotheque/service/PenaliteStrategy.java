package com.unz.bibliotheque.service;

/**
 * DESIGN PATTERN : STRATEGY
 * Interface définissant le contrat de calcul des pénalités.
 * Permet de changer l'algorithme de calcul sans modifier le code client.
 * Principe SOLID : Open/Closed - ouvert à l'extension, fermé à la modification.
 */
public interface PenaliteStrategy {
    /**
     * Calcule le montant de la pénalité en FCFA
     * @param joursRetard nombre de jours de retard
     * @return montant en FCFA
     */
    double calculerPenalite(long joursRetard);
}
