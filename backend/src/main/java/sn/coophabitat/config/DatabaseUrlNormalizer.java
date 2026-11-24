package sn.coophabitat.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Normalise automatiquement DB_URL / DATABASE_URL au format Render/Railway/Neon
 * (postgres://user:pass@host:port/db ou postgresql://...) vers une URL JDBC valide.
 */
public class DatabaseUrlNormalizer implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication application) {
        String raw = firstNonBlank(env.getProperty("DB_URL"), env.getProperty("DATABASE_URL"));
        if (raw == null) return;
        raw = raw.trim();

        Map<String, Object> props = new HashMap<>();
        if (raw.startsWith("postgres://") || raw.startsWith("postgresql://")) {
            try {
                URI uri = URI.create(raw);
                String host = uri.getHost();
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String db = uri.getPath() == null ? "" : uri.getPath();
                String query = uri.getRawQuery();
                StringBuilder jdbc = new StringBuilder("jdbc:postgresql://").append(host).append(":").append(port).append(db);
                if (query != null && !query.isBlank()) {
                    jdbc.append("?").append(query);
                    if (host != null && host.contains(".render.com") && !query.contains("sslmode=")) {
                        jdbc.append("&sslmode=require");
                    }
                } else if (host != null && host.contains(".render.com")) {
                    jdbc.append("?sslmode=require");
                }
                props.put("spring.datasource.url", jdbc.toString());

                String userInfo = uri.getRawUserInfo();
                if (userInfo != null && !userInfo.isBlank()) {
                    int sep = userInfo.indexOf(':');
                    if (sep > 0) {
                        props.put("spring.datasource.username", decode(userInfo.substring(0, sep)));
                        props.put("spring.datasource.password", decode(userInfo.substring(sep + 1)));
                    } else {
                        props.put("spring.datasource.username", decode(userInfo));
                    }
                }
            } catch (Exception ignored) {
                // Laisse Spring Boot signaler l'URL invalide si non parsable
            }
        } else if (raw.startsWith("jdbc:postgresql://")) {
            String fixed = raw;
            if (fixed.contains(".render.com") && !fixed.contains("sslmode=")) {
                fixed += (fixed.contains("?") ? "&" : "?") + "sslmode=require";
            }
            props.put("spring.datasource.url", fixed);
        }

        if (!props.isEmpty()) {
            env.getPropertySources().addFirst(new MapPropertySource("databaseUrlNormalizer", props));
        }
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }

    private static String decode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }
}
