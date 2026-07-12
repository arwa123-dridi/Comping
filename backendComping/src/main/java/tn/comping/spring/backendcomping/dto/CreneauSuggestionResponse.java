package tn.comping.spring.backendcomping.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreneauSuggestionResponse {
    private String creneauLivraisonId;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String raison;
}
