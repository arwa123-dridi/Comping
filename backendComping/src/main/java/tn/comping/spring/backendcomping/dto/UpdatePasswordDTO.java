package tn.comping.spring.backendcomping.dto;

import lombok.Data;
import jakarta.validation.constraints.Size;

@Data
public class UpdatePasswordDTO {

    @Size(min = 6, message = "L'ancien mot de passe doit faire au moins 6 caractères")
    private String oldPassword;

    @Size(min = 6, message = "Le nouveau mot de passe doit faire au moins 6 caractères")
    private String newPassword;

    @Size(min = 6, message = "La confirmation doit faire au moins 6 caractères")
    private String confirmPassword;
}