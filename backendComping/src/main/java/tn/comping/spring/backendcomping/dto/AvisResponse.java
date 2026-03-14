package tn.comping.spring.backendcomping.dto;

import lombok.*;
import java.util.Date;

@Data 
@NoArgsConstructor 
@AllArgsConstructor
public class AvisResponse {
    private String id;
    private String siteCampingId;
    private String utilisateurId;
    private int note;
    private String commentaire;
    private Date dateCreation;
    private String statutModeration;
}