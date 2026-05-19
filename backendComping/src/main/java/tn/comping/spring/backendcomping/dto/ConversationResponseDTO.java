package tn.comping.spring.backendcomping.dto;

import lombok.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
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

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getParticipant1Id() { return participant1Id; }
    public void setParticipant1Id(String participant1Id) { this.participant1Id = participant1Id; }
    public String getParticipant1Nom() { return participant1Nom; }
    public void setParticipant1Nom(String participant1Nom) { this.participant1Nom = participant1Nom; }
    public String getParticipant2Id() { return participant2Id; }
    public void setParticipant2Id(String participant2Id) { this.participant2Id = participant2Id; }
    public String getParticipant2Nom() { return participant2Nom; }
    public void setParticipant2Nom(String participant2Nom) { this.participant2Nom = participant2Nom; }
    public boolean isGroupe() { return groupe; }
    public void setGroupe(boolean groupe) { this.groupe = groupe; }
    public String getNomGroupe() { return nomGroupe; }
    public void setNomGroupe(String nomGroupe) { this.nomGroupe = nomGroupe; }
    public String getAvatarGroupe() { return avatarGroupe; }
    public void setAvatarGroupe(String avatarGroupe) { this.avatarGroupe = avatarGroupe; }
    public List<String> getParticipantIds() { return participantIds; }
    public void setParticipantIds(List<String> participantIds) { this.participantIds = participantIds; }
    public List<String> getParticipantNoms() { return participantNoms; }
    public void setParticipantNoms(List<String> participantNoms) { this.participantNoms = participantNoms; }
    public String getAvisId() { return avisId; }
    public void setAvisId(String avisId) { this.avisId = avisId; }
    public int getMessagesNonLus() { return messagesNonLus; }
    public void setMessagesNonLus(int messagesNonLus) { this.messagesNonLus = messagesNonLus; }
    public Date getDateDernierMessage() { return dateDernierMessage; }
    public void setDateDernierMessage(Date dateDernierMessage) { this.dateDernierMessage = dateDernierMessage; }
    public String getDernierMessageContenu() { return dernierMessageContenu; }
    public void setDernierMessageContenu(String dernierMessageContenu) { this.dernierMessageContenu = dernierMessageContenu; }
    public boolean isAutreParticipantEnLigne() { return autreParticipantEnLigne; }
    public void setAutreParticipantEnLigne(boolean autreParticipantEnLigne) { this.autreParticipantEnLigne = autreParticipantEnLigne; }

    public static ConversationResponseDTOBuilder builder() {
        return new ConversationResponseDTOBuilder();
    }

    public static class ConversationResponseDTOBuilder {
        private ConversationResponseDTO dto = new ConversationResponseDTO();
        public ConversationResponseDTOBuilder id(String id) { dto.setId(id); return this; }
        public ConversationResponseDTOBuilder participant1Id(String participant1Id) { dto.setParticipant1Id(participant1Id); return this; }
        public ConversationResponseDTOBuilder participant2Id(String participant2Id) { dto.setParticipant2Id(participant2Id); return this; }
        public ConversationResponseDTOBuilder groupe(boolean groupe) { dto.setGroupe(groupe); return this; }
        public ConversationResponseDTOBuilder nomGroupe(String nomGroupe) { dto.setNomGroupe(nomGroupe); return this; }
        public ConversationResponseDTOBuilder avatarGroupe(String avatarGroupe) { dto.setAvatarGroupe(avatarGroupe); return this; }
        public ConversationResponseDTOBuilder participantIds(List<String> participantIds) { dto.setParticipantIds(participantIds); return this; }
        public ConversationResponseDTOBuilder dateDernierMessage(Date dateDernierMessage) { dto.setDateDernierMessage(dateDernierMessage); return this; }
        public ConversationResponseDTOBuilder dernierMessageContenu(String dernierMessageContenu) { dto.setDernierMessageContenu(dernierMessageContenu); return this; }
        public ConversationResponseDTO build() { return dto; }
    }
}
