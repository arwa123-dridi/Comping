package tn.comping.spring.backendcomping.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.comping.spring.backendcomping.entities.Role;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.repositories.SignupRepository;

@Configuration
public class AdminInitializer {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

    @Bean
    CommandLineRunner initAdmin(SignupRepository signupRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@comping.tn";
            if (signupRepository.findByEmail(adminEmail).isEmpty()) {
                logger.info("🛠️ Creating default Admin account...");
                
                SignupEntity admin = new SignupEntity();
                admin.setFirstName("Super");
                admin.setLastName("Admin");
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                admin.setTelephone("12345678");
                admin.setAddress("Tunis");
                
                signupRepository.save(admin);
                logger.info("✅ Default Admin account created successfully!");
                logger.info("👉 Email: admin@comping.tn");
                logger.info("👉 Password: admin123");
            } else {
                logger.info("✅ Admin account already exists (admin@comping.tn). Skipping initialization.");
            }
        };
    }
}
