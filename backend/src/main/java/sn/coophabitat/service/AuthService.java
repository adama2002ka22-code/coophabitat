package sn.coophabitat.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import sn.coophabitat.dto.Dtos.AuthResponse;
import sn.coophabitat.dto.Dtos.LoginRequest;
import sn.coophabitat.dto.Dtos.MembreResponse;
import sn.coophabitat.security.JwtService;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MembreService membreService;

    public AuthService(AuthenticationManager am, JwtService jwtService, MembreService membreService) {
        this.authenticationManager = am; this.jwtService = jwtService; this.membreService = membreService;
    }

    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, req.motDePasse()));
        MembreResponse membre = membreService.me(email);
        return new AuthResponse(jwtService.generate(membre.email(), membre.role().name()), "Bearer", membre);
    }
}
