package tn.comping.spring.backendcomping.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeTransportRequest {

    private String typeService;
    private String adresseDepart;
    private String adresseArrivee;
    private LocalDate dateSouhaitee;
    private String description;
}
