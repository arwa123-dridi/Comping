package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
@Document(collection = "Alerte")
public class Alerte {
    @Id
    private String id;
    private TypeAlerte type;
    private String titre;
    private String description;
    private Date dateDeclenchement;
    private String statut;
    private String position;
    private String siteCampingId;
    
    // Enhanced fields for advanced workflows
    private String priorite; // BASSE, MOYENNE, HAUTE, CRITIQUE
    private String assigneId; // ID de la personne assignée
    private Date dateResolution;
    private String resolution; // Description de la résolution
    private Integer responseTimeMinutes; // Temps de réponse en minutes
    private String reporterId; // ID de la personne qui a rapporté l'alerte
    private List<String> affectedUsers; // Liste des utilisateurs affectés
    private String equipmentAffected; // Équipement affecté
    private Double estimatedCost; // Coût estimé de la résolution
    private List<String> attachments; // URLs des fichiers attachés
    private String escalationNotes; // Notes d'escalade
}