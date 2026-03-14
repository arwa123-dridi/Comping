package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
@Document(collection = "SiteCamping")
public class SiteCamping {
    @Id
    private String id;
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
    private double noteMoyenne;
    private String proprietaireId;
}