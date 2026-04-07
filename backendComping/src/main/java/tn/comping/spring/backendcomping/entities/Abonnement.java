package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "abonnements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Abonnement {
    @Id
    private String id;

    private String suiveurId;
    private String suiviId;
    
    // Many-to-many follow system
}
