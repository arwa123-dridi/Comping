package tn.comping.spring.backendcomping.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PanierLigneRequestDTO {

    private String produitId;

    private Integer quantite;
}