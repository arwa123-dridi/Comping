package tn.comping.spring.backendcomping.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
public class MessageRequestDTO {
    private String conversationId;
    private String expediteurId;
    private String contenu;
    private String type; // TEXT, VOICE, IMAGE, FILE
    private String audioUrl;
    private String transcript;

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getExpediteurId() { return expediteurId; }
    public void setExpediteurId(String expediteurId) { this.expediteurId = expediteurId; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
    public String getTranscript() { return transcript; }
    public void setTranscript(String transcript) { this.transcript = transcript; }
}
