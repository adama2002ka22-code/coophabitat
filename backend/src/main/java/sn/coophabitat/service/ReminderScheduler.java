package sn.coophabitat.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Clock;
import java.time.YearMonth;

/** Tache quotidienne : echeances du mois (courant + suivant), penalites de retard, rappels J-3. */
@Component
public class ReminderScheduler {
    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);
    private final CotisationService cotisations;
    private final Clock clock;

    public ReminderScheduler(CotisationService cotisations, Clock clock) { this.cotisations = cotisations; this.clock = clock; }

    @Scheduled(cron = "${app.scheduler.cron}", zone = "Africa/Dakar")
    public void quotidien() {
        YearMonth now = YearMonth.now(clock);
        int g = cotisations.genererEcheances(now) + cotisations.genererEcheances(now.plusMonths(1));
        cotisations.rafraichirPenalites();
        int r = cotisations.envoyerRappels();
        log.info("Tache quotidienne : {} echeances generees, {} rappels J-3 envoyes", g, r);
    }
}
