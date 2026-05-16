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

    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime dateModification = LocalDateTime.now();

    private Integer nbMembresMax;
    private String niveau;

    @DBRef
    private SignupEntity organisateur;

    @DBRef
    @Builder.Default
    private List<SignupEntity> membres = new ArrayList<>();
}