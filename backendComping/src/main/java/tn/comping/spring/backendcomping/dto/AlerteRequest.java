package tn.comping.spring.backendcomping.dto;

import lombok.*;
import tn.comping.spring.backendcomping.entities.TypeAlerte;

@Data 
@NoArgsConstructor 
@AllArgsConstructor
public class AlerteRequest {
    private String siteCampingId;
    private TypeAlerte type;
    private String titre;
    private String description;
    private String position;
}