package tn.comping.spring.backendcomping.entities;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "Incident")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder //pour un constructeur par defaut
@ToString
public class Incident {

    @Id
    private String idIncident;
    private String type;
    private StatutIncident statut;
    private String description;
    private Date dateDeclaration;
    private String userId;

    private PrioriteIncident priorite;
    private String commentaireOrganisateur;
    private LocalDateTime dateTraitement;
    private String demandeTransportId;

    @Builder.Default
    private List<HistoriqueEntry> historique = new ArrayList<>();

    public boolean isResolu() {
        return statut == StatutIncident.RESOLU;
    }
}
