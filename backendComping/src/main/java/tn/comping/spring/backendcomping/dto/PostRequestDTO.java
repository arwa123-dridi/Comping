package tn.comping.spring.backendcomping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import tn.comping.spring.backendcomping.entities.TypePost;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PostRequestDTO {
    
    @NotBlank(message = "Contenu obligatoire pour post TEXTE")
    @Size(max = 2000, message = "Contenu max 2000 caractères")
    private String contenu;
    
    private List<String> images;  // URLs images
    
    @NotNull(message = "TypePost obligatoire")
    private TypePost typePost;
    
    private String avisId;  // Pour PARTAGE_AVIS
}

