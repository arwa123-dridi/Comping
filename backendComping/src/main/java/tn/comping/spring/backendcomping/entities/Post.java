package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Post {
    @Id
    private String id;

    private String auteurId;
    private String typePost; // FEED, STORY, etc.
    
    private String avisId; // Optional link to Avis
    
    private String cibleType; // POST, AVIS
    private String cibleId;
    
    private String contenu;
    private List<String> images;
    
    private Date datePublication;
    
    private int likesCount = 0;
    private int commentairesCount = 0;
}
