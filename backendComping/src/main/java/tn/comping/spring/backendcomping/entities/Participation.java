package tn.comping.spring.backendcomping.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.time.LocalDateTime;

@Document(collection = "participations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Participation {
    @Id
    private String id;

    // IDs simples
    private String utilisateurId;
    private String utilisateurNom;
    private String utilisateurEmail;

    private String sortieId;
    private String sortieTitre;

    private LocalDateTime dateInscription;
    private String statutPresence; // CONFIRME, PRESENT, ABSENT
    private Boolean aValideChecklist;
    private LocalDateTime dateValidation;

    private LocalDateTime dateCreation;
}