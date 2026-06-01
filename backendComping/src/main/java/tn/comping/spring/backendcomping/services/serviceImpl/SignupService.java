package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.LoginDTORequest;
import tn.comping.spring.backendcomping.dto.LoginDTOResponse;
import tn.comping.spring.backendcomping.dto.SignupDTO;
import tn.comping.spring.backendcomping.entities.SignupEntity;

import java.util.List;

public interface SignupService {

    SignupEntity registerUser(SignupDTO dto);

    LoginDTOResponse login(LoginDTORequest request);

    long getTotalUsers();

    SignupEntity getUserById(String id);

    List<SignupEntity> getLivreurs();
}