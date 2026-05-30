package tn.comping.spring.backendcomping.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PanierResponseDTO {

    private String id;

    private String userId;

    private List<PanierLigneResponseDTO> lignes;

    private Double totalPrice;

    private String statut;
}