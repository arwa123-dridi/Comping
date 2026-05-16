package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.comping.spring.backendcomping.config.JwtUtils;
import tn.comping.spring.backendcomping.dto.LoginDTORequest;
import tn.comping.spring.backendcomping.dto.LoginDTOResponse;
import tn.comping.spring.backendcomping.dto.SignupDTO;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.utils.mapper.SignupMapper;


import java.util.List;

import tn.comping.spring.backendcomping.entities.Role;


@Service
@RequiredArgsConstructor
public class SignupServiceImpl implements SignupService {

    private static final Logger logger = LoggerFactory.getLogger(SignupServiceImpl.class);

    private final SignupRepository signupRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public SignupEntity registerUser(SignupDTO dto) {
        String normalizedEmail = dto.getEmail().trim().toLowerCase();
        if (signupRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        SignupEntity user = SignupMapper.toEntity(dto);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        SignupEntity savedUser = signupRepository.save(user);
        logger.info("✅ User added: {}", savedUser);
        return savedUser;
    }

    @Override
    public LoginDTOResponse login(LoginDTORequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        SignupEntity user = signupRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));


        if (!user.isStatut()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED");
        }

        boolean passwordMatches;
        try {
            passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        } catch (Exception e) {
            logger.error("⚠️ Password invalide en DB pour {}", normalizedEmail);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_PASSWORD");
        }

        if (!passwordMatches) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_PASSWORD");
        }

        String token = jwtUtils.generateToken(user.getEmail(), user.getId(), user.getRole());
        return new LoginDTOResponse(token);
    }

    @Override
    public SignupEntity getUserById(String id) {
        return signupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Override
    public List<SignupEntity> getLivreurs() {
        return signupRepository.findByRole(Role.LIVREUR);
    }
}
