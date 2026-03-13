package tn.comping.spring.backendcomping.dto;

import lombok.*;

import java.util.Date;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreneauLivraisonResponse {
    private String idCreneauLivraison;
    private LocalTime heureFin;
    private LocalTime heureDebut;
    private boolean disponible;
}