package tn.comping.spring.backendcomping.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PanierRequestDTO {

    private String userId;

    private List<PanierLigneRequestDTO> lignes;
}