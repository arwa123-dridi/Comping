package tn.comping.spring.backendcomping.dto;

import lombok.*;
import tn.comping.spring.backendcomping.entities.StatutEvent;


@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponseDTO {

    private String idEvent;
    private String titre;
    private String description;
    private double prix;
    private int capacite;
    private StatutEvent statut;
}
