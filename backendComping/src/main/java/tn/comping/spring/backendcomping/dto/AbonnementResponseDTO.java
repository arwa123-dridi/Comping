package tn.comping.spring.backendcomping.dto;

import lombok.*;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AbonnementResponseDTO {
    
    private String id;
    private String suiveurId;
    private String suiveurNom;
    
    private String suiviId;
    private String suiviNom;
    
    private Date dateAbonnement;
}

