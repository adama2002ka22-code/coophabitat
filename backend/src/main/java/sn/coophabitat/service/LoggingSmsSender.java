package sn.coophabitat.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingSmsSender implements SmsSender {
    private static final Logger log = LoggerFactory.getLogger(LoggingSmsSender.class);
    @Override
    public void envoyer(String telephone, String message) {
        if (telephone != null && !telephone.isBlank()) log.info("[SMS -> {}] {}", telephone, message);
    }
}
