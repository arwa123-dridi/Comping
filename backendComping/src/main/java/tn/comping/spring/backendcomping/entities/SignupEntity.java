package tn.comping.spring.backendcomping.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Document(collection = "users")
public class SignupEntity {

    @Id
    private String id;

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String telephone;
    private String address;

    private Role role;

    // profile
    private String photo;
    private boolean statut = true;

    // SOCIAL / PRESENCE FEATURES (ajouté par Mariem)
    private boolean online = false;
    private Date lastSeen;
    private String statusMessage;
}