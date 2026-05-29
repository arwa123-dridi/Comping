package tn.comping.spring.backendcomping.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ParticipationDTO {
    private String id;

    // Optional team assignment (computed from Equipe.membres; backward compatible)
    private String equipeId;
    private String equipeNom;

    
    private String utilisateurId;
    private String utilisateurNom;
    private String utilisateurPrenom;
    private String utilisateurEmail;
    private String sortieId;
    private String sortieTitre;
    private LocalDateTime dateInscription;
    private String statutPresence;
    private Boolean aValideChecklist;
    // ✅ NOUVEAU : message d'information (équipe, etc.)
    private String message;
}