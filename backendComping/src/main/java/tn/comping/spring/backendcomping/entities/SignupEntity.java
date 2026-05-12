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

    private String photo;
    private boolean statut = true;

    // === RESEAU SOCIAL - Statut en ligne ===
    private boolean online = false;
    private Date lastSeen;
    private String statusMessage; // "En randonnée", "Au camping", etc.
}
