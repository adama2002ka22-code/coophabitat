package sn.coophabitat.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sn.coophabitat.domain.Membre;
import sn.coophabitat.domain.StatutMembre;
import sn.coophabitat.repository.MembreRepository;

@Service
public class MembreUserDetailsService implements UserDetailsService {
    private final MembreRepository repository;
    public MembreUserDetailsService(MembreRepository repository) { this.repository = repository; }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Membre m = repository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));
        return User.withUsername(m.getEmail()).password(m.getMotDePasse()).roles(m.getRole().name())
                .disabled(m.getStatut() == StatutMembre.SUSPENDU).build();
    }
}
