package tn.comping.spring.backendcomping.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRequestDTO {
    private String avisId; // Optional
    private String cibleType;
    private String cibleId;
    private String contenu;
    private List<String> images;
}
