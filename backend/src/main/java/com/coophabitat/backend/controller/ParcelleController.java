package com.coophabitat.backend.controller;

import com.coophabitat.backend.entity.Parcelle;
import com.coophabitat.backend.service.ParcelleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/parcelles")
@CrossOrigin(origins = "*")
public class ParcelleController {

    @Autowired
    private ParcelleService parcelleService;

    // 1. Lister toutes les parcelles : GET http://localhost:8080/api/parcelles
    @GetMapping
    public List<Parcelle> getAllParcelles() {
        return parcelleService.getAllParcelles();
    }

    // 2. Trouver une parcelle par son ID : GET http://localhost:8080/api/parcelles/{id}
    @GetMapping("/{id}")
    public Optional<Parcelle> getParcelleById(@PathVariable Long id) {
        return parcelleService.getParcelleById(id);
    }

    // 3. Créer ou ajouter une parcelle : POST http://localhost:8080/api/parcelles
    @PostMapping
    public Parcelle createParcelle(@RequestBody Parcelle parcelle) {
        return parcelleService.saveParcelle(parcelle);
    }

    // 4. Supprimer une parcelle : DELETE http://localhost:8080/api/parcelles/{id}
    @DeleteMapping("/{id}")
    public void deleteParcelle(@PathVariable Long id) {
        parcelleService.deleteParcelle(id);
    }
}