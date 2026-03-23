package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.UpdatePasswordDTO;
import tn.comping.spring.backendcomping.dto.UpdateProfileDTO;
import tn.comping.spring.backendcomping.entities.SignupEntity;

public interface IProfileService {

    /**
     * Récupérer le profil d'un utilisateur
     */
    SignupEntity getProfile(String userId);

    /**
     * Mettre à jour les informations du profil
     */
    SignupEntity updateProfile(String userId, UpdateProfileDTO dto);

    /**
     * Changer le mot de passe
     */
    String updatePassword(String userId, UpdatePasswordDTO dto);

    /**
     * Mettre à jour uniquement la photo
     */
    String updatePhoto(String userId, String photoUrl);


    SignupEntity getUserByEmail(String email);
}