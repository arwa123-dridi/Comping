package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

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
    private String statut;
    private String typeService ;
    private String userId;
}
