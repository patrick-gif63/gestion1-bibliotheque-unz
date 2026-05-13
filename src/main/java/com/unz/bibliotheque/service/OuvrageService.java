package com.unz.bibliotheque.service;

import com.unz.bibliotheque.model.Ouvrage;
import com.unz.bibliotheque.repository.OuvrageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class OuvrageService {

    @Autowired private OuvrageRepository ouvrageRepository;

    public List<Ouvrage> getTousLesOuvrages() {
        return ouvrageRepository.findAll();
    }

    public Ouvrage getOuvrageById(Long id) {
        return ouvrageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Ouvrage non trouvé : " + id));
    }

    public Page<Ouvrage> rechercherOuvrages(String motCle, int page, int taille) {
        Pageable pageable = PageRequest.of(page, taille);
        return ouvrageRepository.rechercherParMotCle(motCle, pageable);
    }

    public Ouvrage ajouterOuvrage(Ouvrage ouvrage) {
        if (ouvrage.getIsbn() != null && ouvrageRepository.findByIsbn(ouvrage.getIsbn()).isPresent()) {
            throw new RuntimeException("Un ouvrage avec cet ISBN existe déjà : " + ouvrage.getIsbn());
        }
        ouvrage.setNbDisponibles(ouvrage.getNbExemplaires());
        return ouvrageRepository.save(ouvrage);
    }

    public Ouvrage modifierOuvrage(Long id, Ouvrage ouvrageMaj) {
        Ouvrage existant = getOuvrageById(id);
        existant.setTitre(ouvrageMaj.getTitre());
        existant.setIsbn(ouvrageMaj.getIsbn());
        existant.setAnneePublication(ouvrageMaj.getAnneePublication());
        existant.setDescription(ouvrageMaj.getDescription());
        existant.setCategorie(ouvrageMaj.getCategorie());
        return ouvrageRepository.save(existant);
    }

    public void supprimerOuvrage(Long id) {
        if (!ouvrageRepository.existsById(id)) {
            throw new RuntimeException("Ouvrage non trouvé : " + id);
        }
        ouvrageRepository.deleteById(id);
    }

    public List<Ouvrage> getTopEmpruntes(int limite) {
        return ouvrageRepository.findTopEmpruntes(PageRequest.of(0, limite));
    }
}
