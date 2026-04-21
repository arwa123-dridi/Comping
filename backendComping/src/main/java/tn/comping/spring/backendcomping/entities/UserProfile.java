package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "userProfiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    private String id;

    // Référence directe vers ton SignupEntity existant
    @DBRef
    private SignupEntity utilisateur;

    // Préférences calculées depuis l'historique
    private String niveauDominant;              // FACILE, MOYEN, DIFFICILE
    private List<String> regionsFrequentes;     // ex: ["Zaghouan", "Ain Draham"]
    private List<String> difficultesFrequentes; // ex: ["MOYEN", "FACILE"]
    private List<String> saisonsPreferees;      // ex: ["PRINTEMPS", "AUTOMNE"]
    private List<String> joursPreferees;        // ex: ["SATURDAY", "SUNDAY"]

    private int nbParticipationsTotal;
    private LocalDateTime derniereMiseAJour;

    // Compteurs bruts pour le calcul
    private Map<String, Integer> regionCount;
    private Map<String, Integer> diffCount;
    private Map<String, Integer> saisonCount;

    // ── Helper : niveau en chiffre ────────────────────────
    public int getNiveauNum() {
        if (niveauDominant == null) return 1;
        return switch (niveauDominant) {
            case "FACILE"    -> 0;
            case "MOYEN"     -> 1;
            case "DIFFICILE" -> 2;
            default          -> 1;
        };
    }
}