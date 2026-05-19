package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tn.comping.spring.backendcomping.dto.IncidentRequest;
import tn.comping.spring.backendcomping.dto.IncidentResponse;
import tn.comping.spring.backendcomping.entities.Incident;
import tn.comping.spring.backendcomping.repositories.IncidentRepository;
import tn.comping.spring.backendcomping.utils.mapper.IncidentMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final WebClient.Builder webClientBuilder;

    private static final String FLASK_API_URL = "http://localhost:5000/predict";

    @Override
    public IncidentResponse createIncident(IncidentRequest dto) {
        Incident entity = IncidentMapper.toEntity(dto);
        
        // Predict severity via Flask API
        try {
            Map<String, Object> response = webClientBuilder.build()
                    .post()
                    .uri(FLASK_API_URL)
                    .bodyValue(Map.of("description", dto.getDescrition()))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (response != null && response.containsKey("severity")) {
                entity.setUrgence(response.get("severity").toString());
            }
        } catch (Exception e) {
            log.warn("Flask AI prediction failed: {}. Falling back to default severity.", e.getMessage());
            entity.setUrgence("MEDIUM");
        }

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

        return IncidentMapper.toDto(incidentRepository.save(existing));
    }

    @Override
    public void deleteIncident(String id) {
        if (!incidentRepository.existsById(id)) {
            throw new RuntimeException("Incident n'existe pas, id: " + id);
        }
        incidentRepository.deleteById(id);
    }
}
