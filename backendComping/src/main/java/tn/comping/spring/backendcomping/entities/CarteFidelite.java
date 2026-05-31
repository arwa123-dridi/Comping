package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "CarteFidelite")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CarteFidelite {

    @Id
    private String id;
    private int points;
    private String niveau;
    private String clientId;
}