package com.coophabitat.backend.controller;

import com.coophabitat.backend.entity.Membre;
import com.coophabitat.backend.repository.MembreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/membres")
@CrossOrigin(origins = "http://localhost:4200") // Autorise ton front-end Angular
public class MembreController {

    @Autowired
    private MembreRepository membreRepository;

    @GetMapping
    public List<Membre> getAllMembres() {
        return membreRepository.findAll();
    }

    @PostMapping
    public Membre createMembre(@RequestBody Membre membre) {
        return membreRepository.save(membre);
    }
}