package tn.comping.spring.backendcomping.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ParticipationDTO {

    private String id;
    private String utilisateurId;
    private String utilisateurNom;
    private String utilisateurPrenom; // ✅ ADD THIS
    private String utilisateurEmail;  // ✅ optional (if needed)

    private String sortieId;
    private String sortieTitre;
    private LocalDateTime dateInscription;
    private String statutPresence;
    private Boolean aValideChecklist;

}