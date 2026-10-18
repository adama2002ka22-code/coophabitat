package com.coophabitat.backend.service;

import com.coophabitat.backend.entity.User;
import com.coophabitat.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Récupérer tous les utilisateurs (pour le tableau de bord de l'administrateur)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Trouver un utilisateur par son ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Enregistrer un nouvel utilisateur ou un membre
    public User saveUser(User user) {
        // Ici, on pourra ajouter plus tard l'encryptage du mot de passe (BCrypt)
        return userRepository.save(user);
    }

    // Supprimer un utilisateur
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}