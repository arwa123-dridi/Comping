package tn.comping.spring.backendcomping.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;  // ✅ NOUVEAU
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.time.LocalDateTime;
import tn.comping.spring.backendcomping.entities.SignupEntity;  // ✅
import tn.comping.spring.backendcomping.entities.Sortie;        // ✅

@Document(collection = "participations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Participation {
    @Id
    private String id;

    // ✅ NOUVEAU : Référence à l'utilisateur
    @DBRef
    private SignupEntity utilisateur;

    // ✅ NOUVEAU : Référence à la sortie
    @DBRef
    private Sortie sortie;

    // ✅ GARDÉ : Attributs de la relation
    private LocalDateTime dateInscription;
    private String statutPresence; // CONFIRME, PRESENT, ABSENT
    private Boolean aValideChecklist;
    private LocalDateTime dateValidation;
    private LocalDateTime dateCreation;

    // ❌ SUPPRIMÉ : private String utilisateurId;
    // ❌ SUPPRIMÉ : private String utilisateurNom;
    // ❌ SUPPRIMÉ : private String utilisateurEmail;
    // ❌ SUPPRIMÉ : private String sortieId;
    // ❌ SUPPRIMÉ : private String sortieTitre;
}