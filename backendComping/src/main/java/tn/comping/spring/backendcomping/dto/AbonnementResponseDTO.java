package tn.comping.spring.backendcomping.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbonnementResponseDTO {
    private String id;
    private String suiviId;
    private String suiviNom;
    private String suiviEmail;
}
