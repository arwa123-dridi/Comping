package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.comping.spring.backendcomping.dto.UpdatePasswordDTO;
import tn.comping.spring.backendcomping.dto.UpdateProfileDTO;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.repositories.SignupRepository;

import java.util.List;
//import tn.comping.spring.backendcomping.services.serviceImpl.IProfileService; // ✅ AJOUTÉ

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProfileServiceImpl implements IProfileService { //✅ IMPLEMENTS l'interface

    private final SignupRepository signupRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public SignupEntity getProfile(String userId) {
        return signupRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    @Override
    public SignupEntity updateProfile(String userId, UpdateProfileDTO dto) {
        SignupEntity user = signupRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Mise à jour du prenom
        if (dto.getFirstName() != null && !dto.getFirstName().isEmpty()) {
            user.setFirstName(dto.getFirstName());
           
        }

          // Mise à jour du nom
        if (dto.getLastName() != null && !((String) dto.getLastName()).isEmpty()) {
            user.setLastName((String) dto.getLastName());
           
        }

        // Mise à jour de l'email (avec vérification d'unicité)
        if (dto.getEmail() != null && !dto.getEmail().isEmpty() && !dto.getEmail().equals(user.getEmail())) {
            if (signupRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new RuntimeException("Cet email est déjà utilisé");
            }
            user.setEmail(dto.getEmail());
        }

        // Mise à jour du téléphone
        if (dto.getTelephone() != null) {
            user.setTelephone(dto.getTelephone());
        }

        // Mise à jour de l'adresse
        if (dto.getAddress() != null) {
            user.setAddress(dto.getAddress());
        }

        // Mise à jour de la photo
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

        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Ancien mot de passe incorrect");
        }

        // Vérifier que le nouveau mot de passe et la confirmation correspondent
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Les mots de passe ne correspondent pas");
        }

        // Mettre à jour le mot de passe
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

        log.info("Photo mise à jour pour l'utilisateur: {}", userId);
        return "Photo mise à jour avec succès";
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
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé : " + userId));
        signupRepository.delete(user);
    }

    @Override
    public SignupEntity updateStatus(String id, boolean statut) {
        SignupEntity user = signupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatut(statut);

        return signupRepository.save(user);
    }
}