package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.config.JwtUtils;
import tn.comping.spring.backendcomping.dto.LoginDTORequest;
import tn.comping.spring.backendcomping.dto.LoginDTOResponse;
import tn.comping.spring.backendcomping.dto.SignupDTO;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.utils.mapper.SignupMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tn.comping.spring.backendcomping.entities.SignupEntity;

@AllArgsConstructor
    @Service
    public class SignupServiceImpl implements SignupService {

        private SignupRepository signupRepository;
         private  final JwtUtils jwtUtils;
        private final PasswordEncoder passwordEncoder;
        private static final Logger logger = LoggerFactory.getLogger(SignupServiceImpl.class);

        @Override
        public SignupEntity registerUser(SignupDTO dto) {
            if (signupRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new RuntimeException("Email already exists");
            }
            SignupEntity user = SignupMapper.toEntity(dto);
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            // Save user in DB
            SignupEntity savedUser = signupRepository.save(user);
            // Console log
            if (savedUser != null && savedUser.getId() != null) {
                logger.info("✅ User successfully added to the DB: {}", savedUser);
            } else {
                logger.error("❌ Failed to add user to the DB");
            }
            return savedUser;
        }

        @Override
        public LoginDTOResponse login(LoginDTORequest request) {
            SignupEntity user = signupRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
                throw new RuntimeException("Invalid password");
            }

            String token = jwtUtils.generateToken(user.getEmail(),user.getRole());

            return new LoginDTOResponse(token);
        }
        }


