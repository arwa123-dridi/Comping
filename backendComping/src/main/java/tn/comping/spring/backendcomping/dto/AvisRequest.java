package tn.comping.spring.backendcomping.dto;

import lombok.*;

@Data 
@NoArgsConstructor 
@AllArgsConstructor
@Builder   // ✅ REQUIRED for builder()
public class AvisRequest {
    private String siteCampingId;
    private String utilisateurId;
    private int note;
    private String commentaire;
    private String itineraire;
    private String convention;
}