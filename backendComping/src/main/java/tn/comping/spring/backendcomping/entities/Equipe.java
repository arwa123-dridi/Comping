package tn.comping.spring.backendcomping.entities;

import org.springframework.data.annotation.Id;
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
    private String niveau; // DEBUTANT, INTERMEDIAIRE, EXPERT

    // L'organisateur (celui qui a créé l'équipe)
    private String organisateurId;
    private String organisateurNom;

    // Liste des IDs des membres
    @Builder.Default
    private List<String> membreIds = new ArrayList<>();
    private LocalDateTime dateModification;
}