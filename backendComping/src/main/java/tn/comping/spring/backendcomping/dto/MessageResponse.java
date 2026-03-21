package tn.comping.spring.backendcomping.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {
    @Schema(description = "Message de réponse (succès ou erreur)", example = "Lien de réinitialisation envoyé à votre email.")
    private String message;
}