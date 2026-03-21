package tn.comping.spring.backendcomping.utils.mapper;

import tn.comping.spring.backendcomping.dto.CreneauLivraisonRequest;
import tn.comping.spring.backendcomping.dto.CreneauLivraisonResponse;
import tn.comping.spring.backendcomping.entities.CreneauLivraison;

import java.time.LocalTime;

public class CreneauLivraisonMapper {

    public static CreneauLivraison toEntity(CreneauLivraisonRequest dto) {
        if (dto == null) return null;

        return CreneauLivraison.builder()
                .heureDebut(dto.getHeureDebut() != null ? dto.getHeureDebut() : LocalTime.now())
                .heureFin(dto.getHeureFin())
                .disponible(dto.isDisponible())
                .build();
    }

    public static CreneauLivraisonResponse toDto(CreneauLivraison entity) {
        if (entity == null) return null;

        return CreneauLivraisonResponse.builder()
                .idCreneauLivraison(entity.getIdCreneauLivraison())
                .heureDebut(entity.getHeureDebut())
                .heureFin(entity.getHeureFin())
                .disponible(entity.isDisponible())
                .build();
    }
}