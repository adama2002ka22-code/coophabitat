package sn.coophabitat;

import org.junit.jupiter.api.Test;
import sn.coophabitat.domain.Cotisation;
import sn.coophabitat.domain.Membre;
import sn.coophabitat.repository.CotisationRepository;
import sn.coophabitat.repository.MembreRepository;
import sn.coophabitat.repository.PaiementRepository;
import sn.coophabitat.service.CotisationService;
import sn.coophabitat.service.NotificationService;
import sn.coophabitat.service.PenaltyCalculator;
import sn.coophabitat.service.PlanCotisationService;
import sn.coophabitat.service.ReminderScheduler;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReminderSchedulerTest {

    @Test
    void rappelEnvoyeExactementTroisJoursAvantEcheanceEtIdempotent() {
        CotisationRepository cotRepo = mock(CotisationRepository.class);
        MembreRepository membreRepo = mock(MembreRepository.class);
        PaiementRepository payRepo = mock(PaiementRepository.class);
        PlanCotisationService plans = mock(PlanCotisationService.class);
        NotificationService notifs = mock(NotificationService.class);
        Clock fixedClock = Clock.fixed(Instant.parse("2026-05-02T07:00:00Z"), ZoneOffset.UTC);

        CotisationService service = new CotisationService(
                cotRepo, membreRepo, payRepo, plans, new PenaltyCalculator(), notifs, fixedClock);

        Membre m1 = new Membre();
        m1.setId(1L); m1.setNom("Coumbel Ka"); m1.setTelephone("+221778452011"); m1.setEmail("coumbel@coophabitat.sn");

        Cotisation dueJ3 = new Cotisation();
        dueJ3.setMembre(m1);
        dueJ3.setDateEcheance(LocalDate.of(2026, 5, 5)); // J+3 exactement
        dueJ3.setMontantDu(new BigDecimal("50000"));
        dueJ3.setMontantPaye(BigDecimal.ZERO);
        dueJ3.setRappelEnvoye(false);

        Cotisation dejaPayee = new Cotisation();
        dejaPayee.setMembre(m1);
        dejaPayee.setDateEcheance(LocalDate.of(2026, 5, 5));
        dejaPayee.setMontantDu(new BigDecimal("50000"));
        dejaPayee.setMontantPaye(new BigDecimal("50000"));
        dejaPayee.setRappelEnvoye(false);

        when(cotRepo.findByDateEcheanceAndRappelEnvoyeFalse(LocalDate.of(2026, 5, 5)))
                .thenReturn(List.of(dueJ3, dejaPayee));

        int envoyes = service.envoyerRappels();

        assertEquals(1, envoyes);
        assertTrue(dueJ3.isRappelEnvoye());
        assertFalse(dejaPayee.isRappelEnvoye());
        verify(notifs, times(1)).notifier(eq(m1), contains("Rappel"), contains("50000 FCFA"));
    }

    @Test
    void schedulerQuotidienExecuteGenerationPenalitesEtRappels() {
        CotisationService cotisations = mock(CotisationService.class);
        Clock fixedClock = Clock.fixed(Instant.parse("2026-05-02T07:00:00Z"), ZoneOffset.UTC);
        when(cotisations.genererEcheances(any(YearMonth.class))).thenReturn(2);
        when(cotisations.envoyerRappels()).thenReturn(1);

        ReminderScheduler scheduler = new ReminderScheduler(cotisations, fixedClock);
        scheduler.quotidien();

        verify(cotisations).genererEcheances(YearMonth.of(2026, 5));
        verify(cotisations).genererEcheances(YearMonth.of(2026, 6));
        verify(cotisations).rafraichirPenalites();
        verify(cotisations).envoyerRappels();
    }
}
