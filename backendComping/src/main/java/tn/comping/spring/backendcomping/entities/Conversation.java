package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Conversation {
    @Id
    private String id;

    // === MODE 1:1 (legacy) ===
    private String participant1Id;
    private String participant2Id;

    // === MODE GROUPE (min 2 participants) ===
    @Builder.Default
    private boolean groupe = false;
    private String nomGroupe;
    private String avatarGroupe;

    @Builder.Default
    private List<String> participantIds = new ArrayList<>(); // utilisé pour les groupes

    // Unread counts par participant (email -> count)
    @Builder.Default
    private Map<String, Integer> messagesNonLusParParticipant = new HashMap<>();

    // Legacy 1:1 fields (conservés pour compatibilité)
    @Builder.Default
    private int messagesNonLusP1 = 0;
    @Builder.Default
    private int messagesNonLusP2 = 0;

    private String avisId;
    private Date dateDernierMessage;
    private String dernierMessageContenu;

    // Créateur du groupe
    private String createurId;
    private Date dateCreation;
}
