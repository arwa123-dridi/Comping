package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.dto.ForgotPasswordRequest;
import tn.comping.spring.backendcomping.dto.ResetPasswordRequest;
import tn.comping.spring.backendcomping.dto.MessageResponse;
import tn.comping.spring.backendcomping.entities.PasswordResetToken;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.repositories.PasswordResetTokenRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.services.serviceImpl.PasswordResetService;
import tn.comping.spring.backendcomping.services.serviceImpl.EmailService;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final SignupRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public MessageResponse requestPasswordReset(ForgotPasswordRequest request) {
        SignupEntity user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        tokenRepository.findByUtilisateurEmailAndUtiliseFalse(request.getEmail())
                .ifPresent(tokenRepository::delete);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUtilisateurEmail(request.getEmail());
        resetToken.setDateCreation(LocalDateTime.now());
        resetToken.setDateExpiration(LocalDateTime.now().plusHours(1));
        resetToken.setUtilise(false);
        tokenRepository.save(resetToken);

        String resetUrl = "http://localhost:4200/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(request.getEmail(), token, resetUrl);

        return new MessageResponse("Lien de réinitialisation envoyé à votre email.");
    }

    @Override
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        if (token.isUtilise()) {
            throw new RuntimeException("Token déjà utilisé");
        }
        if (token.getDateExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expiré");
        }

        SignupEntity user = userRepository.findByEmail(token.getUtilisateurEmail().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        user.setPassword(passwordEncoder.encode(request.getNouveauMotDePasse()));
        userRepository.save(user);

        token.setUtilise(true);
        token.setDateUtilisation(LocalDateTime.now());
        tokenRepository.save(token);
        tokenRepository.delete(token);

        return new MessageResponse("Mot de passe réinitialisé avec succès.");
    }
}
