package tn.comping.spring.backendcomping.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Document(collection = "equipes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipe {
    @Id
    private String id;

    private String nom;
    private String description;
    private LocalDateTime dateCreation;
    private Integer nbMembresMax;
    private String niveau;

    // ✅ Référence à l'organisateur
    @DBRef
    private SignupEntity organisateur;

    // ✅ Liste des références des membres
    @DBRef
    @Builder.Default
    private List<SignupEntity> membres = new ArrayList<>();

    //  Garder pour compatibilité avec les anciennes requêtes
    @Builder.Default
    private List<String> membreIds = new ArrayList<>();

    private LocalDateTime dateModification;
}
