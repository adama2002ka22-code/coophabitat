package sn.coophabitat.security;

import org.springframework.security.core.Authentication;

public final class SecurityUtils {
    private SecurityUtils() {}
    public static boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
    public static boolean isBureau(Authentication auth) {
        return hasRole(auth, "PRESIDENT") || hasRole(auth, "TRESORIER") || hasRole(auth, "SECRETAIRE");
    }
}
