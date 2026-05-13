package com.unz.bibliotheque.controller;

import com.unz.bibliotheque.model.Ouvrage;
import com.unz.bibliotheque.service.OuvrageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ouvrages")
public class OuvrageController {

    @Autowired
    private OuvrageService ouvrageService;

    // GET /api/ouvrages?motCle=java&page=0&taille=10
    @GetMapping
    public ResponseEntity<Page<Ouvrage>> rechercherOuvrages(
            @RequestParam(defaultValue = "") String motCle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int taille) {
        return ResponseEntity.ok(ouvrageService.rechercherOuvrages(motCle, page, taille));
    }

    // GET /api/ouvrages/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getOuvrage(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ouvrageService.getOuvrageById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // POST /api/ouvrages — Ajouter un ouvrage (bibliothécaire/admin)
    @PostMapping
    @PreAuthorize("hasRole('BIBLIOTHECAIRE') or hasRole('ADMINISTRATEUR')")
    public ResponseEntity<?> ajouterOuvrage(@RequestBody Ouvrage ouvrage) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(ouvrageService.ajouterOuvrage(ouvrage));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // PUT /api/ouvrages/{id} — Modifier un ouvrage
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE') or hasRole('ADMINISTRATEUR')")
    public ResponseEntity<?> modifierOuvrage(@PathVariable Long id, @RequestBody Ouvrage ouvrage) {
        try {
            return ResponseEntity.ok(ouvrageService.modifierOuvrage(id, ouvrage));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/ouvrages/{id} — Supprimer un ouvrage (admin uniquement)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<Void> supprimerOuvrage(@PathVariable Long id) {
        try {
            ouvrageService.supprimerOuvrage(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
