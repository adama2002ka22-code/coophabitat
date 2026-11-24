package sn.coophabitat.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sn.coophabitat.domain.Membre;
import sn.coophabitat.domain.Notification;
import sn.coophabitat.domain.StatutMembre;
import sn.coophabitat.dto.Dtos.NotificationDto;
import sn.coophabitat.dto.Mapper;
import sn.coophabitat.repository.MembreRepository;
import sn.coophabitat.repository.NotificationRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/** Notifications multi-canaux : in-app (toujours), e-mail (si SMTP configure), SMS (via SmsSender). */
@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationRepository repo;
    private final MembreRepository membres;
    private final ObjectProvider<JavaMailSender> mail;
    private final SmsSender sms;
    private final Clock clock;
    private final String from;

    public NotificationService(NotificationRepository repo, MembreRepository membres, ObjectProvider<JavaMailSender> mail,
                               SmsSender sms, Clock clock, @Value("${app.mail.from}") String from) {
        this.repo = repo; this.membres = membres; this.mail = mail; this.sms = sms; this.clock = clock; this.from = from;
    }

    @Transactional
    public void notifier(Membre m, String sujet, String message) {
        Notification n = new Notification();
        n.setDestinataire(m);
        n.setSujet(sujet);
        n.setMessage(message);
        n.setDateCreation(LocalDateTime.now(clock));
        repo.save(n);
        envoyerEmail(m, sujet, message);
        sms.envoyer(m.getTelephone(), sujet + " - " + message);
    }

    @Transactional
    public void notifierTous(String sujet, String message) {
        for (Membre m : membres.findByStatut(StatutMembre.ACTIF)) notifier(m, sujet, message);
    }

    private void envoyerEmail(Membre m, String sujet, String message) {
        JavaMailSender sender = mail.getIfAvailable();
        if (sender == null) return;
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(m.getEmail());
            msg.setSubject(sujet);
            msg.setText(message);
            sender.send(msg);
        } catch (Exception e) {
            log.warn("Echec d'envoi e-mail a {} : {}", m.getEmail(), e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> mesNotifications(Long membreId) {
        return repo.findTop100ByDestinataireIdOrderByDateCreationDesc(membreId).stream().map(Mapper::notification).toList();
    }

    @Transactional
    public void marquerLu(Long id, Long membreId) {
        Notification n = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification introuvable"));
        if (!n.getDestinataire().getId().equals(membreId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acces refuse");
        n.setLu(true);
    }
}
