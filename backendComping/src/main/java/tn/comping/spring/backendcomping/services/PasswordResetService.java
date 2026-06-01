package tn.comping.spring.backendcomping.services;

import tn.comping.spring.backendcomping.dto.ForgotPasswordRequestDTO;
import tn.comping.spring.backendcomping.dto.ResetPasswordRequestDTO;

public interface PasswordResetService {

    String requestPasswordReset(ForgotPasswordRequestDTO requestDTO);

    String resetPassword(ResetPasswordRequestDTO requestDTO);

}

