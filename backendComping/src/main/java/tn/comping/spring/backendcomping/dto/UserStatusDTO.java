package tn.comping.spring.backendcomping.dto;

import lombok.*;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
public class UserStatusDTO {
    private String userId;       // email ou id
    private String nom;
    private boolean online;
    private Date lastSeen;
    private String statusMessage;

    public UserStatusDTO(String userId, boolean online, Date lastSeen) {
        this.userId = userId;
        this.online = online;
        this.lastSeen = lastSeen;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }
    public Date getLastSeen() { return lastSeen; }
    public void setLastSeen(Date lastSeen) { this.lastSeen = lastSeen; }
    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }
}
