package tn.comping.spring.backendcomping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRecommandation {
    private String idEvent;       // pour retrouver l'event complet
    private String titre;         // nom de l'event
    private String lieu;          // où il se passe
    private double prix;          // le prix
    private String raison;        // ⭐ explication de l'IA : "pourquoi cet event te correspond"
    private double scoreMatch;    // ⭐ ex: 0.92 = 92% compatible avec le profil user
}
