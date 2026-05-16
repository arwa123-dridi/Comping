package tn.comping.spring.backendcomping.dto;

import lombok.Data;

@Data
public class LoginDTORequest {
    private String email;
    private String password;
}
