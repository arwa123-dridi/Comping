package tn.comping.spring.backendcomping.dto;

import lombok.*;
import tn.comping.spring.backendcomping.entities.Role;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupDTO {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String telephone;
    private String address;
    private Role role;
}