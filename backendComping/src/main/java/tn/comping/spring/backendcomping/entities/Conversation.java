package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "conversations")
public class Conversation {
    @Id
    private String id;

    // === MODE 1:1 (legacy) ===
    private String participant1Id;
    private String participant2Id;

    // === MODE GROUPE (min 2 participants) ===
    private boolean groupe = false;
    private String nomGroupe;
    private String avatarGroupe;

    private List<String> participantIds = new ArrayList<>(); // utilisé pour les groupes

    // Unread counts par participant (email -> count)
    private Map<String, Integer> messagesNonLusParParticipant = new HashMap<>();

    // Legacy 1:1 fields (conservés pour compatibilité)
    private int messagesNonLusP1 = 0;
    private int messagesNonLusP2 = 0;

    private String avisId;
    private Date dateDernierMessage;
    private String dernierMessageContenu;

    // Créateur du groupe
    private String createurId;
    private Date dateCreation;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getParticipant1Id() { return participant1Id; }
    public void setParticipant1Id(String participant1Id) { this.participant1Id = participant1Id; }
    public String getParticipant2Id() { return participant2Id; }
    public void setParticipant2Id(String participant2Id) { this.participant2Id = participant2Id; }
    public boolean isGroupe() { return groupe; }
    public void setGroupe(boolean groupe) { this.groupe = groupe; }
    public String getNomGroupe() { return nomGroupe; }
    public void setNomGroupe(String nomGroupe) { this.nomGroupe = nomGroupe; }
    public String getAvatarGroupe() { return avatarGroupe; }
    public void setAvatarGroupe(String avatarGroupe) { this.avatarGroupe = avatarGroupe; }
    public List<String> getParticipantIds() { return participantIds; }
    public void setParticipantIds(List<String> participantIds) { this.participantIds = participantIds; }
    public Map<String, Integer> getMessagesNonLusParParticipant() { return messagesNonLusParParticipant; }
    public void setMessagesNonLusParParticipant(Map<String, Integer> messagesNonLusParParticipant) { this.messagesNonLusParParticipant = messagesNonLusParParticipant; }
    public int getMessagesNonLusP1() { return messagesNonLusP1; }
    public void setMessagesNonLusP1(int messagesNonLusP1) { this.messagesNonLusP1 = messagesNonLusP1; }
    public int getMessagesNonLusP2() { return messagesNonLusP2; }
    public void setMessagesNonLusP2(int messagesNonLusP2) { this.messagesNonLusP2 = messagesNonLusP2; }
    public String getAvisId() { return avisId; }
    public void setAvisId(String avisId) { this.avisId = avisId; }
    public Date getDateDernierMessage() { return dateDernierMessage; }
    public void setDateDernierMessage(Date dateDernierMessage) { this.dateDernierMessage = dateDernierMessage; }
    public String getDernierMessageContenu() { return dernierMessageContenu; }
    public void setDernierMessageContenu(String dernierMessageContenu) { this.dernierMessageContenu = dernierMessageContenu; }
    public String getCreateurId() { return createurId; }
    public void setCreateurId(String createurId) { this.createurId = createurId; }
    public Date getDateCreation() { return dateCreation; }
    public void setDateCreation(Date dateCreation) { this.dateCreation = dateCreation; }
}
