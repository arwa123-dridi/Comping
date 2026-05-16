package tn.comping.spring.backendcomping.utils.mapper;

import tn.comping.spring.backendcomping.entities.SignupEntity;

import tn.comping.spring.backendcomping.dto.SignupDTO;

public class SignupMapper {

    // Convert DTO to Entity
    public static SignupEntity toEntity(SignupDTO dto) {
        if (dto == null) return null;

        return SignupEntity.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getlastName())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .telephone(dto.getTelephone())
                .address(dto.getAddress())
                .role(dto.getRole())
                .build();
    }

    // Convert Entity to DTO
    public static SignupDTO toDTO(SignupEntity entity) {
        if (entity == null) return null;

        return SignupDTO.builder()
<<<<<<< HEAD

                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())

=======
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
>>>>>>> origin/ahmed
                .email(entity.getEmail())
                .password(entity.getPassword())
                .telephone(entity.getTelephone())
                .address(entity.getAddress())
                .role(entity.getRole())
                .build();
    }
}