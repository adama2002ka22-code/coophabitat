package com.coophabitat.backend.service;

import com.coophabitat.backend.entity.Paiement;
import com.coophabitat.backend.repository.PaiementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaiementService {

    @Autowired
    private PaiementRepository paiementRepository;

    // Récupérer tous les paiements (pour la comptabilité)
    public List<Paiement> getAllPaiements() {
        return paiementRepository.findAll();
    }

    // Récupérer les paiements d'un membre en particulier
    public List<Paiement> getPaiementsByMembre(Long membreId) {
        return paiementRepository.findByMembreId(membreId);
    }

    // Enregistrer un nouveau paiement / cotisation
    public Paiement savePaiement(Paiement paiement) {
        return paiementRepository.save(paiement);
    }

    // Supprimer un paiement
    public void deletePaiement(Long id) {
        paiementRepository.deleteById(id);
    }
}