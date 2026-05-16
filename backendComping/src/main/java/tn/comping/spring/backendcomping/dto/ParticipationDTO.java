package tn.comping.spring.backendcomping.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ParticipationDTO {

    private String id;
    private String utilisateurId;
    private String utilisateurNom;
<<<<<<< HEAD
    private String utilisateurPrenom;
    private String utilisateurEmail;
=======
    private String utilisateurPrenom; // ✅ ADD THIS
    private String utilisateurEmail;  // ✅ optional (if needed)

>>>>>>> origin/ahmed
    private String sortieId;
    private String sortieTitre;
    private LocalDateTime dateInscription;
    private String statutPresence;
    private Boolean aValideChecklist;
<<<<<<< HEAD
=======

>>>>>>> origin/ahmed
}