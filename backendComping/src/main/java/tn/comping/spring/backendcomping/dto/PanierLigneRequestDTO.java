package tn.comping.spring.backendcomping.dto;

import java.time.LocalDateTime;

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