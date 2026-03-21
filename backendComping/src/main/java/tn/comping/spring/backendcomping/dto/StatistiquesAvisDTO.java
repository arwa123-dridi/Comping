package tn.comping.spring.backendcomping.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatistiquesAvisDTO {

    private long nombreTotal;
    private double noteMoyenne;
    private long nombre5Etoiles;
    private long nombre4Etoiles;
    private long nombre3Etoiles;
    private long nombre2Etoiles;
    private long nombre1Etoile;

}