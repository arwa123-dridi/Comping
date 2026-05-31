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
    private List<String> interests;
    private String niveauExperience;
    private String localisation;
    private double budget;
    private String meteo;
    private String saison;
}