package tn.comping.spring.backendcomping.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequestDTO {

    private String email;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

