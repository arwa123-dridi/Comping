package tn.comping.spring.backendcomping.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Data
public class UpdateProfileDTO {

    @JsonProperty("prenom")
    @Size(min = 2, max = 50, message = "Le prénom doit faire entre 2 et 50 caractères")
    private String firstName;

    @JsonProperty("nom")
    @Size(min = 2, max = 50, message = "Le nom doit faire entre 2 et 50 caractères")
    private String lastName;

    @Email(message = "Email invalide")
    private String email;

    @Size(min = 8, max = 15, message = "Téléphone invalide")
    private String telephone;

    @JsonProperty("adresse")
    private String address;

    private String photo;
}