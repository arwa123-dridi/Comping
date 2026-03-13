package tn.comping.spring.backendcomping.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginDTOResponse {
    private String token;
}
