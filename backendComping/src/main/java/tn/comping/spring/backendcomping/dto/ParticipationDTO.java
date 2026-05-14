package tn.comping.spring.backendcomping.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ParticipationDTO {
    private String id;
    private String utilisateurId;
    private String utilisateurNom;
    private String utilisateurPrenom;
    private String utilisateurEmail;
    private String sortieId;
    private String sortieTitre;
    private LocalDateTime dateInscription;
    private String statutPresence;
    private Boolean aValideChecklist;
}