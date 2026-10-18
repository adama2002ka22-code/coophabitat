package com.coophabitat.backend.controller;

import com.coophabitat.backend.entity.Cotisation;
import com.coophabitat.backend.repository.CotisationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cotisations")
@CrossOrigin(origins = "http://localhost:4200")
public class CotisationController {

    @Autowired
    private CotisationRepository cotisationRepository;

    @GetMapping
    public List<Cotisation> getAllCotisations() {
        return cotisationRepository.findAll();
    }

    @PostMapping
    public Cotisation createCotisation(@RequestBody Cotisation cotisation) {
        return cotisationRepository.save(cotisation);
    }
}