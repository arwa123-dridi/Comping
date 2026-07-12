package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "DemandeTransport")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder //pour un constructeur par defaut
@ToString
public class DemandeTransport {

    @Id
    private String idDemandeTransport;
    private Date dateCreation;
    private StatutDemandeTransport statut;
    private String typeService;
    private String userId;

    private String adresseDepart;
    private String adresseArrivee;
    private LocalDate dateSouhaitee;
    private String description;

    private String creneauLivraisonId;
    private String commentaireOrganisateur;
    private LocalDateTime dateTraitement;
    private String livreurId;

    @Builder.Default
    private List<HistoriqueEntry> historique = new ArrayList<>();

    private boolean noteAttribuee;
}
