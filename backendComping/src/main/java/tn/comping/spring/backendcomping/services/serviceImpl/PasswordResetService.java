package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.ForgotPasswordRequest;
import tn.comping.spring.backendcomping.dto.ResetPasswordRequest;
import tn.comping.spring.backendcomping.dto.MessageResponse;

public interface PasswordResetService {
    MessageResponse requestPasswordReset(ForgotPasswordRequest request);
    MessageResponse resetPassword(ResetPasswordRequest request);
}

