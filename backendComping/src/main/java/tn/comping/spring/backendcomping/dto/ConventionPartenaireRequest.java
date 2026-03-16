package tn.comping.spring.backendcomping.dto;

import lombok.*;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConventionPartenaireRequest {
    private Date dateDebut;
    private Date dateFin;
    private double remise;
    private String conditions;
}