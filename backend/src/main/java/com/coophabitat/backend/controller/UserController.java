package com.coophabitat.backend.controller;

import com.coophabitat.backend.entity.User;
import com.coophabitat.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // Permettra à Angular de communiquer facilement avec le backend
public class UserController {

    @Autowired
    private UserService userService;

    // 1. Afficher tous les utilisateurs : GET http://localhost:8080/api/users
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // 2. Trouver un utilisateur par son ID : GET http://localhost:8080/api/users/{id}
    @GetMapping("/{id}")
    public Optional<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // 3. Créer ou inscrire un utilisateur : POST http://localhost:8080/api/users
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.saveUser(user);
    }

    // 4. Supprimer un utilisateur : DELETE http://localhost:8080/api/users/{id}
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}