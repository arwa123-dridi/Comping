package tn.comping.spring.backendcomping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tn.comping.spring.backendcomping.entities.TypeMessage;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MessageRequestDTO {
    
    @NotBlank(message = "Contenu message obligatoire")
    private String contenu;
    
    @NotNull(message = "Type message obligatoire")
    private TypeMessage typeMessage;
}

