package tn.comping.spring.backendcomping.dto;

import lombok.*;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponseDTO {
    private String id;
    private String auteurNom;
    private String typePost;
    private String avisId;
    private String cibleType;
    private String cibleId;
    private String contenu;
    private List<String> images;
    private Date datePublication;
    private int likesCount;
    private int commentairesCount;
}
