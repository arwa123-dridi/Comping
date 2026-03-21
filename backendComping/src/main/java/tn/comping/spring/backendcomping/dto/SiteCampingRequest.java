package tn.comping.spring.backendcomping.dto;

import lombok.*;
import java.util.List;

@Data 
@NoArgsConstructor 
@AllArgsConstructor
public class SiteCampingRequest {
    private String nom;
    private String description;
    private String localisation;
    private double latitude;
    private double longitude;
    private int capacite;
    private double tarifs;
    private boolean disponible;
    private String consignesSecurite;
    private List<String> photos;
    private String proprietaireId;
}