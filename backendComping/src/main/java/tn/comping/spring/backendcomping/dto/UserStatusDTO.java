package tn.comping.spring.backendcomping.dto;

import lombok.*;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatusDTO {
    private String userId;       // email ou id
    private String nom;
    private boolean online;
    private Date lastSeen;
    private String statusMessage;
}
