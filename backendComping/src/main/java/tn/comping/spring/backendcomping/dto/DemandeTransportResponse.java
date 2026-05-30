package tn.comping.spring.backendcomping.dto;

import lombok.*;

import java.util.Date;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeTransportResponse{

    private String idDemandeTransport;
    private Date dateCreation;
    private String statut;
    private String typeService;
    private String userId;
}