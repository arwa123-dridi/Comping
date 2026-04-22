package tn.comping.spring.backendcomping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {
    private String userId;
    private int age;
    private List<String> interests;      // ["sport", "culture", "nature"]
    private String niveauExperience;     // "debutant" | "intermediaire" | "expert"
    private String localisation;         // "Tunis"
    private double budget;
    private String meteo;                // "ensoleille" | "nuageux" | "interieur"
}
