package tn.comping.spring.backendcomping.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Document(collection = "users") // MongoDB collection name
public class SignupEntity {

    @Id
    private String id; // MongoDB ObjectId, String is typical

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String telephone;
    private String address;
    private Role role;

    private String photo;// URL de la photo
<<<<<<< HEAD
    @Builder.Default
    private boolean statut = true;




=======
    private boolean statut = true;
>>>>>>> origin/ahmed

  
 

  

   
}