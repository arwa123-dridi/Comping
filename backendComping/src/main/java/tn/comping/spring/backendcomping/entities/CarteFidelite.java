package tn.comping.spring.backendcomping.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "CarteFidelite")
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CarteFidelite {
    @Id
    private String id;

    private int points;

    private String niveau;

    private String clientId;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
}
