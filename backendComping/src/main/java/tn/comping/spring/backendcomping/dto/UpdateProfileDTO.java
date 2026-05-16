package tn.comping.spring.backendcomping.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Data
public class UpdateProfileDTO {

        @Size(min = 2, max = 50, message = "Le prénom doit faire entre 2 et 50 caractères")
        private String firstName;

        @Size(min = 2, max = 50, message = "Le nom doit faire entre 2 et 50 caractères")
        private String lastName;

        @Email(message = "Email invalide")
        private String email;

        @Size(min = 8, max = 15, message = "Téléphone invalide")
        private String telephone;

<<<<<<< HEAD
        private String address;
        private String photo;
    }
=======
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
>>>>>>> origin/ahmed
