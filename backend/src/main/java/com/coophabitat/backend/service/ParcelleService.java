package com.coophabitat.backend.service;

import com.coophabitat.backend.entity.Parcelle;
import com.coophabitat.backend.repository.ParcelleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ParcelleService {

    @Autowired
    private ParcelleRepository parcelleRepository;

    // Récupérer toutes les parcelles
    public List<Parcelle> getAllParcelles() {
        return parcelleRepository.findAll();
    }

    // Récupérer une parcelle par son ID
    public Optional<Parcelle> getParcelleById(Long id) {
        return parcelleRepository.findById(id);
    }

    // Enregistrer ou mettre à jour une parcelle
    public Parcelle saveParcelle(Parcelle parcelle) {
        return parcelleRepository.save(parcelle);
    }

    // Supprimer une parcelle
    public void deleteParcelle(Long id) {
        parcelleRepository.deleteById(id);
    }
}