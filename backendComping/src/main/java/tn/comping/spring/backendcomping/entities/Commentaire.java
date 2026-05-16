package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "commentaires")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Commentaire {
    @Id
    private String id;
    
    private String postId; // Référence au post parent
    
    private String parentCommentId; // NULL pour commentaire root, ID sinon
    private String auteurId;
    private String contenu;
    
    private Date datePublication;
    @Builder.Default
    private boolean valide = true;

    @Builder.Default
    private int likesCount = 0;

    // Pour l'ordre d'affichage
    @Builder.Default
    private int niveau = 0; // 0=réponse directe, 1=réponse à réponse, etc.
}
