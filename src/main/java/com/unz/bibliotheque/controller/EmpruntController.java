package com.unz.bibliotheque.controller;

import com.unz.bibliotheque.model.Emprunt;
import com.unz.bibliotheque.service.EmpruntService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Contrôleur REST pour la gestion des emprunts.
 * Endpoints sécurisés selon les rôles.
 */
@RestController
@RequestMapping("/api/emprunts")
public class EmpruntController {

    @Autowired
    private EmpruntService empruntService;

    // POST /api/emprunts — Créer un emprunt (bibliothécaire/admin uniquement)
    @PostMapping
    @PreAuthorize("hasRole('BIBLIOTHECAIRE') or hasRole('ADMINISTRATEUR')")
    public ResponseEntity<?> creerEmprunt(
            @RequestParam Long ouvrageId,
            @RequestParam Long etudiantId) {
        try {
            Emprunt emprunt = empruntService.creerEmprunt(ouvrageId, etudiantId);
            return ResponseEntity.status(HttpStatus.CREATED).body(emprunt);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // PUT /api/emprunts/{id}/retour — Enregistrer le retour
    @PutMapping("/{id}/retour")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE') or hasRole('ADMINISTRATEUR')")
    public ResponseEntity<?> enregistrerRetour(@PathVariable Long id) {
        try {
            Emprunt emprunt = empruntService.enregistrerRetour(id);
            return ResponseEntity.ok(emprunt);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // GET /api/emprunts/etudiant/{id} — Historique d'un étudiant
    @GetMapping("/etudiant/{id}")
    @PreAuthorize("hasRole('ETUDIANT') or hasRole('BIBLIOTHECAIRE') or hasRole('ADMINISTRATEUR')")
    public ResponseEntity<List<Emprunt>> getEmpruntsEtudiant(@PathVariable Long id) {
        return ResponseEntity.ok(empruntService.getEmpruntsEtudiant(id));
    }

    // GET /api/emprunts — Tous les emprunts (admin/bibliothécaire)
    @GetMapping
    @PreAuthorize("hasRole('BIBLIOTHECAIRE') or hasRole('ADMINISTRATEUR')")
    public ResponseEntity<List<Emprunt>> getTousLesEmprunts() {
        return ResponseEntity.ok(empruntService.getTousLesEmprunts());
    }

    // GET /api/emprunts/retard — Emprunts en retard
    @GetMapping("/retard")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE') or hasRole('ADMINISTRATEUR')")
    public ResponseEntity<List<Emprunt>> getEmpruntsEnRetard() {
        return ResponseEntity.ok(empruntService.getEmpruntsEnRetard());
    }
}
