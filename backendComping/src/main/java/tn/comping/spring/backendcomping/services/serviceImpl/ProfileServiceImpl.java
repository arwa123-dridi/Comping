package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.comping.spring.backendcomping.dto.UpdatePasswordDTO;
import tn.comping.spring.backendcomping.dto.UpdateProfileDTO;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.repositories.SignupRepository;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProfileServiceImpl implements IProfileService {

    private final SignupRepository signupRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    @Override
    public SignupEntity getProfile(String userId) {
        return signupRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    @Override
    public SignupEntity updateProfile(String userId, UpdateProfileDTO dto) {
        SignupEntity user = signupRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (dto.getFirstName() != null && !dto.getFirstName().isEmpty()) {
            user.setFirstName(dto.getFirstName());
        }

        if (dto.getLastName() != null && !dto.getLastName().toString().isEmpty()) {
            user.setLastName(dto.getLastName().toString());
        }

        if (dto.getEmail() != null && !dto.getEmail().isEmpty() && !dto.getEmail().equals(user.getEmail())) {
            if (signupRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new RuntimeException("Cet email est déjà utilisé");
            }
            user.setEmail(dto.getEmail());
        }

        if (dto.getTelephone() != null) {
            user.setTelephone(dto.getTelephone());
        }

        if (dto.getAddress() != null) {
            user.setAddress(dto.getAddress());
        }

        if (dto.getPhoto() != null) {
            user.setPhoto(dto.getPhoto());
        }

        log.info("Profil mis à jour pour l'utilisateur: {}", userId);
        return signupRepository.save(user);
    }

    @Override
    public String updatePassword(String userId, UpdatePasswordDTO dto) {
        SignupEntity user = signupRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Ancien mot de passe incorrect");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Les mots de passe ne correspondent pas");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        signupRepository.save(user);

        log.info("Mot de passe changé pour l'utilisateur: {}", userId);
        return "Mot de passe modifié avec succès";
    }

    @Override
    public String updatePhoto(String userId, String photoUrl) {
        SignupEntity user = signupRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        user.setPhoto(photoUrl);
        signupRepository.save(user);
        return "Photo mise à jour";
    }

    @Override
    public String updateAvatar(String userId, MultipartFile file) {
        SignupEntity user = signupRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        try {
            String photoUrl = cloudinaryService.uploadImage(file);
            user.setPhoto(photoUrl);
            signupRepository.save(user);
            return photoUrl;
        } catch (IOException e) {
            log.error("Erreur lors de l'upload de l'avatar: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de l'upload de l'image");
        }
    }

    @Override
    public SignupEntity getUserByEmail(String email) {
        return signupRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    @Override
    public List<SignupEntity> getAllUsers() {
        return signupRepository.findAll();
    }

    @Override
    public void deleteUser(String userId) {
        SignupEntity user = signupRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        user.setStatut(false); // Soft delete
        signupRepository.save(user);
    }

    @Override
    public SignupEntity updateStatus(String id, boolean statut) {
        SignupEntity user = signupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        user.setStatut(statut);
        return signupRepository.save(user);
    }
}
