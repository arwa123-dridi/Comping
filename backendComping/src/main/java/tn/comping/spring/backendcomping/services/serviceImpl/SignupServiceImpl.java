package tn.comping.spring.backendcomping.services.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.dto.SignupDTO;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.utils.mapper.SignupMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tn.comping.spring.backendcomping.entities.SignupEntity;


    @Service
    public class SignupServiceImpl implements SignupService {

        @Autowired
        private SignupRepository signupRepository;

        private static final Logger logger = LoggerFactory.getLogger(SignupServiceImpl.class);

        @Override
        public SignupEntity registerUser(SignupDTO dto) {
            // Check if email already exists
            if (signupRepository.findByEmail(dto.getEmail()) != null) {
                throw new RuntimeException("Email already exists");
            }

            // Convert DTO to Entity using Mapper
            SignupEntity user = SignupMapper.toEntity(dto);

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
    }

