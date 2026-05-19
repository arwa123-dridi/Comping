package tn.comping.spring.backendcomping.services;

import tn.comping.spring.backendcomping.dto.UpdatePasswordDTO;
import tn.comping.spring.backendcomping.dto.UpdateProfileDTO;
import tn.comping.spring.backendcomping.entities.SignupEntity;

import java.util.List;

public interface IProfileService {

    SignupEntity getProfile(String userId);

    SignupEntity updateProfile(String userId, UpdateProfileDTO dto);

    String updatePassword(String userId, UpdatePasswordDTO dto);

    String updatePhoto(String userId, String photoUrl);

    SignupEntity getUserByEmail(String email);

    List<SignupEntity> getAllUsers();

    void deleteUser(String userId);
    SignupEntity updateStatus(String id, boolean statut);
}