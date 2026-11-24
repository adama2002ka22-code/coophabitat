package sn.coophabitat.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity @Table(name = "notifications")
@Getter @Setter @NoArgsConstructor
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "destinataire_id") private Membre destinataire;
    @Column(nullable = false) private String sujet;
    @Column(length = 2000) private String message;
    @Column(nullable = false) private LocalDateTime dateCreation;
    @Column(nullable = false) private boolean lu = false;
}
