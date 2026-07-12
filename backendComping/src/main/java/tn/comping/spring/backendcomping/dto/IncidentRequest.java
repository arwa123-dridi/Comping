package tn.comping.spring.backendcomping.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentRequest {
    private String type;
    private String description;
    private String demandeTransportId;
}
