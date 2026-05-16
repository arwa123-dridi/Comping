package tn.comping.spring.backendcomping.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRequestDTO {
    private String avisId;
    private String cibleType;
    private String cibleId;
    private String contenu;
    private List<String> images;
    private String visibilite; // PUBLIC, AMIS, PRIVE
}
