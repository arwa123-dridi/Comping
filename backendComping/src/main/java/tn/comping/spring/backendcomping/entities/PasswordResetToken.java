package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.UUID;

@Document(collection = "password_reset_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PasswordResetToken {

    @Id
    private String id;

    private String userId;
    private String token = UUID.randomUUID().toString();
    private Date expiryDate;
    private boolean used = false;

}

