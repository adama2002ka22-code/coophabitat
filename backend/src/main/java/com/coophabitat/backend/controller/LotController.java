package com.coophabitat.backend.controller;

import com.coophabitat.backend.entity.Lot;
import com.coophabitat.backend.repository.LotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lots")
@CrossOrigin(origins = "http://localhost:4200")
public class LotController {

    @Autowired
    private LotRepository lotRepository;

    @GetMapping
    public List<Lot> getAllLots() {
        return lotRepository.findAll();
    }

    @PostMapping
    public Lot createLot(@RequestBody Lot lot) {
        return lotRepository.save(lot);
    }
}