package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "messages")
public class Message {
    @Id
    private String id;

    private String conversationId;
    
    private String expediteurId;
    private String destinataireId;
    
    private String contenu;
    private String typeMessage; // TEXT, IMAGE, etc.
    
    private boolean lu = false;
    private boolean supprime = false;
    
    private Date dateCreation;
    
    private String transcription; // Pour messages vocaux transcrits (VosK)
    
    private String callData; // WebRTC signaling: offer/answer/ICE candidates JSON

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getExpediteurId() { return expediteurId; }
    public void setExpediteurId(String expediteurId) { this.expediteurId = expediteurId; }
    public String getDestinataireId() { return destinataireId; }
    public void setDestinataireId(String destinataireId) { this.destinataireId = destinataireId; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public String getTypeMessage() { return typeMessage; }
    public void setTypeMessage(String typeMessage) { this.typeMessage = typeMessage; }
    public boolean isLu() { return lu; }
    public void setLu(boolean lu) { this.lu = lu; }
    public boolean isSupprime() { return supprime; }
    public void setSupprime(boolean supprime) { this.supprime = supprime; }
    public Date getDateCreation() { return dateCreation; }
    public void setDateCreation(Date dateCreation) { this.dateCreation = dateCreation; }
    public String getTranscription() { return transcription; }
    public void setTranscription(String transcription) { this.transcription = transcription; }
    public String getCallData() { return callData; }
    public void setCallData(String callData) { this.callData = callData; }

    public static MessageBuilder builder() {
        return new MessageBuilder();
    }

    public static class MessageBuilder {
        private Message message = new Message();
        public MessageBuilder id(String id) { message.setId(id); return this; }
        public MessageBuilder conversationId(String conversationId) { message.setConversationId(conversationId); return this; }
        public MessageBuilder expediteurId(String expediteurId) { message.setExpediteurId(expediteurId); return this; }
        public MessageBuilder destinataireId(String destinataireId) { message.setDestinataireId(destinataireId); return this; }
        public MessageBuilder contenu(String contenu) { message.setContenu(contenu); return this; }
        public MessageBuilder typeMessage(String typeMessage) { message.setTypeMessage(typeMessage); return this; }
        public MessageBuilder dateCreation(Date dateCreation) { message.setDateCreation(dateCreation); return this; }
        public MessageBuilder transcription(String transcription) { message.setTranscription(transcription); return this; }
        public Message build() { return message; }
    }
}
