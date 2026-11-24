package sn.coophabitat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import sn.coophabitat.config.DatabaseUrlNormalizer;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseUrlNormalizerTest {
    private final DatabaseUrlNormalizer normalizer = new DatabaseUrlNormalizer();

    @Test
    void convertitUrlInterneRenderPostgresqlEnJdbc() {
        MockEnvironment env = new MockEnvironment()
                .withProperty("DB_URL", "postgresql://coophabitat_api_user:secret123@dpg-abc123-a/coophabitat_api");
        normalizer.postProcessEnvironment(env, null);
        assertEquals("jdbc:postgresql://dpg-abc123-a:5432/coophabitat_api", env.getProperty("spring.datasource.url"));
        assertEquals("coophabitat_api_user", env.getProperty("spring.datasource.username"));
        assertEquals("secret123", env.getProperty("spring.datasource.password"));
    }

    @Test
    void convertitUrlExterneRenderEtAjouteSsl() {
        MockEnvironment env = new MockEnvironment()
                .withProperty("DATABASE_URL", "postgres://user:pass@dpg-abc-a.oregon-postgres.render.com/mydb");
        normalizer.postProcessEnvironment(env, null);
        assertEquals("jdbc:postgresql://dpg-abc-a.oregon-postgres.render.com:5432/mydb?sslmode=require",
                env.getProperty("spring.datasource.url"));
        assertEquals("user", env.getProperty("spring.datasource.username"));
        assertEquals("pass", env.getProperty("spring.datasource.password"));
    }

    @Test
    void ajouteSslSurUrlJdbcExterneRenderSansSsl() {
        MockEnvironment env = new MockEnvironment()
                .withProperty("DB_URL", "jdbc:postgresql://dpg-abc-a.oregon-postgres.render.com:5432/mydb");
        normalizer.postProcessEnvironment(env, null);
        assertEquals("jdbc:postgresql://dpg-abc-a.oregon-postgres.render.com:5432/mydb?sslmode=require",
                env.getProperty("spring.datasource.url"));
    }
}
