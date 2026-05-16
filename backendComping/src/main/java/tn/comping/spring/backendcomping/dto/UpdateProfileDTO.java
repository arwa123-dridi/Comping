package tn.comping.spring.backendcomping.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Data
public class UpdateProfileDTO {

    @Size(min = 3, max = 50, message = "Le nom doit faire entre 3 et 50 caractères")
    private String name;

    @Email(message = "Email invalide")
    private String email;

    @Size(min = 8, max = 15, message = "Téléphone invalide")
    private String telephone;

    private String address;

    private String photo; // nouvelle photo

    public Object getLastName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLastName'");
    }

    public String getFirstName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFirstName'");
    }
}