package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.dto.IncidentRequest;
import tn.comping.spring.backendcomping.dto.IncidentResponse;
import tn.comping.spring.backendcomping.entities.Incident;
import tn.comping.spring.backendcomping.repositories.IncidentRepository;
import tn.comping.spring.backendcomping.utils.mapper.IncidentMapper;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;

    @Override
    public IncidentResponse createIncident(IncidentRequest dto) {
        Incident entity = IncidentMapper.toEntity(dto);
        return IncidentMapper.toDto(incidentRepository.save(entity));
    }

    @Override
    public IncidentResponse getIncidentById(String id) {
        Incident entity = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident n'existe pas, id: " + id));
        return IncidentMapper.toDto(entity);
    }

    @Override
    public List<IncidentResponse> getAllIncidents() {
        return incidentRepository.findAll()
                .stream()
                .map(IncidentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public IncidentResponse updateIncident(String id, IncidentRequest dto) {
        Incident existing = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident n'existe pas, id: " + id));
        existing.setType(dto.getType() != null ? dto.getType() : existing.getType());
        existing.setStatut(dto.getStatut() != null ? dto.getStatut() : existing.getStatut());
        existing.setDescrition(dto.getDescrition() != null ? dto.getDescrition() : existing.getDescrition());
        existing.setDateDeclaration(dto.getDateDeclaration() != null ? dto.getDateDeclaration() : existing.getDateDeclaration());
        existing.setResolu(dto.isResolu());
        existing.setUserId(dto.getUserId() != null ? dto.getUserId() : existing.getUserId());
        return IncidentMapper.toDto(incidentRepository.save(existing));
    }

    @Override
    public void deleteIncident(String id) {
        if (!incidentRepository.existsById(id)) {
            throw new RuntimeException("Incident n'existe pas, id: " + id);
        }
        incidentRepository.deleteById(id);
    }

    @Override
    public List<IncidentResponse> getIncidentsByUserId(String userId) {
        return incidentRepository.findByUserId(userId)
                .stream()
                .map(IncidentMapper::toDto)
                .collect(Collectors.toList());
    }
}