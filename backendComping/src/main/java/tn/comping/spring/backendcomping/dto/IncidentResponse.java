package tn.comping.spring.backendcomping.dto;

import lombok.*;

import java.util.Date;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentResponse {
    private String idIncident;
    private String type;
    private String statut;
    private String descrition;
    private Date dateDeclaration;
    private boolean resolu;
    private String userId;
}