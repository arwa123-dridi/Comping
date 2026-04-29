package tn.comping.spring.backendcomping.dto;
import tn.comping.spring.backendcomping.dto.AlerteResponse;

import lombok.*;
import tn.comping.spring.backendcomping.entities.TypeAlerte;

import java.util.Date;

@Data 
@NoArgsConstructor 
@AllArgsConstructor
public class AlerteResponse {
    private String id;
    private String siteCampingId;
    private TypeAlerte type;
    private String titre;
    private String description;
    private Date dateDeclenchement;
    private String statut;
    private String position;
}