package tn.comping.spring.backendcomping.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForgotPasswordRequestDTO {

    private String email;

}

