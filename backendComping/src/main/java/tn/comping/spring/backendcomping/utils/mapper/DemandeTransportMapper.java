package tn.comping.spring.backendcomping.utils.mapper;

import tn.comping.spring.backendcomping.dto.DemandeTransportRequest;
import tn.comping.spring.backendcomping.dto.DemandeTransportResponse;
import tn.comping.spring.backendcomping.entities.DemandeTransport;
import tn.comping.spring.backendcomping.entities.StatutDemandeTransport;

import java.util.ArrayList;
import java.util.Date;

public class DemandeTransportMapper {

    public static DemandeTransport toEntity(DemandeTransportRequest dto, String userId) {
        if (dto == null) return null;

        return DemandeTransport.builder()
                .dateCreation(new Date())
                .statut(StatutDemandeTransport.EN_ATTENTE)
                .typeService(dto.getTypeService())
                .adresseDepart(dto.getAdresseDepart())
                .adresseArrivee(dto.getAdresseArrivee())
                .dateSouhaitee(dto.getDateSouhaitee())
                .description(dto.getDescription())
                .userId(userId)
                .historique(new ArrayList<>())
                .noteAttribuee(false)
                .build();
    }

    public static void updateEntityFromDto(DemandeTransport entity, DemandeTransportRequest dto) {
        entity.setTypeService(dto.getTypeService());
        entity.setAdresseDepart(dto.getAdresseDepart());
        entity.setAdresseArrivee(dto.getAdresseArrivee());
        entity.setDateSouhaitee(dto.getDateSouhaitee());
        entity.setDescription(dto.getDescription());
    }

    public static DemandeTransportResponse toDto(DemandeTransport entity) {
        if (entity == null) return null;

        return DemandeTransportResponse.builder()
                .idDemandeTransport(entity.getIdDemandeTransport())
                .dateCreation(entity.getDateCreation())
                .statut(entity.getStatut())
                .typeService(entity.getTypeService())
                .userId(entity.getUserId())
                .adresseDepart(entity.getAdresseDepart())
                .adresseArrivee(entity.getAdresseArrivee())
                .dateSouhaitee(entity.getDateSouhaitee())
                .description(entity.getDescription())
                .creneauLivraisonId(entity.getCreneauLivraisonId())
                .commentaireOrganisateur(entity.getCommentaireOrganisateur())
                .dateTraitement(entity.getDateTraitement())
                .livreurId(entity.getLivreurId())
                .historique(entity.getHistorique())
                .noteAttribuee(entity.isNoteAttribuee())
                .build();
    }
}
