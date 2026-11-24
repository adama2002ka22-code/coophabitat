package sn.coophabitat.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sn.coophabitat.dto.Dtos.*;
import sn.coophabitat.service.AuthService;
import sn.coophabitat.service.MembreService;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification")
public class AuthController {
    private final AuthService authService;
    private final MembreService membreService;
    public AuthController(AuthService authService, MembreService membreService) {
        this.authService = authService; this.membreService = membreService;
    }

    @Operation(summary = "Inscription d'un membre (role MEMBRE uniquement)")
    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    public MembreResponse register(@Valid @RequestBody RegisterRequest req) { return membreService.register(req); }

    @Operation(summary = "Connexion : retourne un jeton JWT")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) { return authService.login(req); }
}
