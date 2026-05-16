package tn.comping.spring.backendcomping.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "CarteFidelite")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder //pour un constructeur par defaut
@ToString
@Entity
public class CarteFidelite {
    @Id
    private String id;

    private int points;

    private String niveau;

    private String clientId;
}
