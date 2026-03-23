package tn.comping.spring.backendcomping.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityRequest {

    private String nom;
    private String description;
    private String type;
    private String duree;
    private String capacite;
}
