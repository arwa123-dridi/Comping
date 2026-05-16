package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.entities.Alerte;
import tn.comping.spring.backendcomping.repositories.AlerteRepository;
import tn.comping.spring.backendcomping.utils.mapper.AlerteMapper;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlerteServiceImpl implements AlerteService {

    private static final String STATUT_ACTIVE = "ACTIVE";
    private static final String STATUT_EN_COURS = "EN_COURS";
    private static final String STATUT_CLOTUREE = "CLOTUREE";

    private final AlerteRepository repository;
    private final AlerteMapper mapper;

    @Override
    public AlerteResponse declencherAlerte(AlerteRequest request) {
        Alerte alerte = mapper.toEntity(request);
        alerte.setDateDeclenchement(new Date());
        alerte.setStatut(STATUT_ACTIVE);
        return mapper.toResponse(repository.save(alerte));
    }

    @Override
    public List<AlerteResponse> getAlertesBySite(String siteCampingId) {
        return repository.findBySiteCampingId(siteCampingId).stream()
                .map(mapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<AlerteResponse> getAlertesByCritere(String siteCampingId, String statut) {
        if (siteCampingId != null && !siteCampingId.isBlank() && statut != null && !statut.isBlank()) {
            return repository.findBySiteCampingIdAndStatut(siteCampingId, statut).stream()
                    .map(mapper::toResponse).collect(Collectors.toList());
        }

        if (siteCampingId != null && !siteCampingId.isBlank()) {
            return repository.findBySiteCampingId(siteCampingId).stream()
                    .map(mapper::toResponse).collect(Collectors.toList());
        }

        if (statut != null && !statut.isBlank()) {
            return repository.findByStatut(statut).stream()
                    .map(mapper::toResponse).collect(Collectors.toList());
        }

        return getAllAlertes();
    }

    @Override
    public List<AlerteResponse> getAllAlertes() {
        return repository.findAll().stream()
                .map(mapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public void supprimerAlerte(String id) {
        repository.deleteById(id);
    }

    @Override
    public AlerteResponse updateStatut(String id, String statut) {
        Alerte alerte = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alerte non trouvée : " + id));
        alerte.setStatut(statut);
        return mapper.toResponse(repository.save(alerte));
    }

    @Override
    public AlerteResponse prendreEnCharge(String id) {
        return updateStatut(id, STATUT_EN_COURS);
    }

    @Override
    public AlerteResponse cloturer(String id) {
        return updateStatut(id, STATUT_CLOTUREE);
    }
}