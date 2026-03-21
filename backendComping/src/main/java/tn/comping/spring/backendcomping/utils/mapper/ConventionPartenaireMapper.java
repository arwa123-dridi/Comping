package tn.comping.spring.backendcomping.utils.mapper;

import tn.comping.spring.backendcomping.dto.ConventionPartenaireRequest;
import tn.comping.spring.backendcomping.dto.ConventionPartenaireResponse;
import tn.comping.spring.backendcomping.entities.ConventionPartenaire;
import java.util.Date;

public class ConventionPartenaireMapper {

    public static ConventionPartenaire toEntity(ConventionPartenaireRequest dto) {
        if (dto == null) return null;

        return ConventionPartenaire.builder()
                .dateDebut(dto.getDateDebut() != null ? dto.getDateDebut() : new Date())
                .dateFin(dto.getDateFin())
                .remise(dto.getRemise())
                .conditions(dto.getConditions())
                .build();
    }

    public static ConventionPartenaireResponse toDto(ConventionPartenaire entity) {
        if (entity == null) return null;

        return ConventionPartenaireResponse.builder()
                .idConventionPartenaire(entity.getIdConventionPartenaire())
                .dateDebut(entity.getDateDebut())
                .dateFin(entity.getDateFin())
                .remise(entity.getRemise())
                .conditions(entity.getConditions())
                .build();
    }
}