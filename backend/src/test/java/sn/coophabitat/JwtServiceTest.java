package sn.coophabitat;

import org.junit.jupiter.api.Test;
import sn.coophabitat.security.JwtService;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private static final String SECRET = "test-secret-test-secret-test-secret-test-secret";
    private final JwtService service = new JwtService(SECRET, 5);

    @Test void tokenValideRetourneLEmail() {
        assertEquals("a@b.sn", service.validateAndGetEmail(service.generate("a@b.sn", "MEMBRE")).orElseThrow());
    }
    @Test void tokenAltereEstRejete() {
        assertTrue(service.validateAndGetEmail(service.generate("a@b.sn", "MEMBRE") + "x").isEmpty());
    }
    @Test void tokenExpireEstRejete() {
        assertTrue(service.validateAndGetEmail(new JwtService(SECRET, -1).generate("a@b.sn", "MEMBRE")).isEmpty());
    }
    @Test void secretTropCourtRefuse() {
        assertThrows(IllegalStateException.class, () -> new JwtService("court", 5));
    }
}
