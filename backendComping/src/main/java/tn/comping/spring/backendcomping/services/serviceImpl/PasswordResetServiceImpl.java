package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.dto.ForgotPasswordRequestDTO;
import tn.comping.spring.backendcomping.dto.ResetPasswordRequestDTO;
import tn.comping.spring.backendcomping.entities.PasswordResetToken;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.repositories.PasswordResetTokenRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.services.PasswordResetService;

import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private final SignupRepository signupRepository;
    private final PasswordResetTokenRepository tokenRepository;
private final PasswordEncoder passwordEncoder;
private final EmailService emailService;

    @Override
    @Transactional
    public String requestPasswordReset(ForgotPasswordRequestDTO requestDTO) {
        SignupEntity user = signupRepository.findByEmail(requestDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Supprime anciens tokens
        List<PasswordResetToken> oldTokens = tokenRepository.findByUserId(user.getId());
        tokenRepository.deleteAll(oldTokens);

        // Crée nouveau token (1h expire)
        PasswordResetToken token = PasswordResetToken.builder()
                .userId(user.getId())
                .token(UUID.randomUUID().toString())
                .expiryDate(Date.from(Instant.now().plusSeconds(3600))) // 1h
                .build();
        tokenRepository.save(token);

        log.info("Token reset créé pour user: {}", user.getEmail());

        // Envoi email
        emailService.sendPasswordResetEmail(user.getEmail(), token.getToken(), user.getFirstName());

        return "Email envoyé avec token: " + token.getToken();
    }

    @Override
    @Transactional
    public String resetPassword(ResetPasswordRequestDTO requestDTO) {
        PasswordResetToken token = tokenRepository.findByToken(requestDTO.getToken())
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        if (token.isUsed()) {
            throw new RuntimeException("Token déjà utilisé");
        }

        if (token.getExpiryDate().before(new Date())) {
            throw new RuntimeException("Token expiré");
        }

        SignupEntity user = signupRepository.findById(token.getUserId())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Hash nouveau password
        user.setPassword(passwordEncoder.encode(requestDTO.getNouveauMotDePasse()));
        signupRepository.save(user);

        // Marque token utilisé
        token.setUsed(true);
        tokenRepository.save(token);

        log.info("Password reset pour user: {}", user.getEmail());

        return "Mot de passe réinitialisé avec succès";
    }

}

