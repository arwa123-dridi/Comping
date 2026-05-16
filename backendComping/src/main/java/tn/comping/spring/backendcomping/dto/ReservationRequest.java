package tn.comping.spring.backendcomping.dto;

import lombok.*;
import java.util.Date;

@Data @NoArgsConstructor @AllArgsConstructor
public class ReservationRequest {
    private String siteCampingId;
    private String utilisateurId;
    private Date dateDebut;
    private Date dateFin;
    private String modePaiement;
    private Integer nombrePersonnes=1;
}