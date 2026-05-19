package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "abonnements")
@NoArgsConstructor
@AllArgsConstructor
public class Abonnement {
    @Id
    private String id;

    private String suiveurId;
    private String suiviId;
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSuiveurId() { return suiveurId; }
    public void setSuiveurId(String suiveurId) { this.suiveurId = suiveurId; }
    public String getSuiviId() { return suiviId; }
    public void setSuiviId(String suiviId) { this.suiviId = suiviId; }

    public static AbonnementBuilder builder() {
        return new AbonnementBuilder();
    }

    public static class AbonnementBuilder {
        private Abonnement abonnement = new Abonnement();
        public AbonnementBuilder id(String id) { abonnement.setId(id); return this; }
        public AbonnementBuilder suiveurId(String suiveurId) { abonnement.setSuiveurId(suiveurId); return this; }
        public AbonnementBuilder suiviId(String suiviId) { abonnement.setSuiviId(suiviId); return this; }
        public Abonnement build() { return abonnement; }
    }
}
