package tn.comping.spring.backendcomping.entities;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

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
    private String statut;
    private String descrition;
    private Date dateDeclaration;
    private boolean resolu;
    private String userId;
}
