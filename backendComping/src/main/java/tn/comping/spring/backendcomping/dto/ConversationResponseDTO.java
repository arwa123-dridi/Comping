package tn.comping.spring.backendcomping.dto;

import lombok.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationResponseDTO {
    private String id;

    // 1:1
    private String participant1Id;
    private String participant1Nom;
    private String participant2Id;
    private String participant2Nom;

    // Groupe
    private boolean groupe;
    private String nomGroupe;
    private String avatarGroupe;
    private List<String> participantIds;
    private List<String> participantNoms;

    private String avisId;
    private int messagesNonLus;
    private Date dateDernierMessage;
    private String dernierMessageContenu;

    // Statut en ligne de l'autre participant (pour 1:1)
    private boolean autreParticipantEnLigne;
}
