package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.dto.DemandeTransportRequest;
import tn.comping.spring.backendcomping.dto.DemandeTransportResponse;
import tn.comping.spring.backendcomping.entities.DemandeTransport;
import tn.comping.spring.backendcomping.repositories.DemandeTransportRepository;
import tn.comping.spring.backendcomping.utils.mapper.DemandeTransportMapper;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DemandeTransportServiceImpl implements DemandeTransportService {

    private final DemandeTransportRepository demandeTransportRepository;

    @Override
    public DemandeTransportResponse createDemandeTransport(DemandeTransportRequest dto) {
        DemandeTransport entity = DemandeTransportMapper.toEntity(dto);
        return DemandeTransportMapper.toDto(demandeTransportRepository.save(entity));
    }

    @Override
    public DemandeTransportResponse getDemandeTransportById(String id) {
        DemandeTransport entity = demandeTransportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DemandeTransport n'existe pas, id: " + id));
        return DemandeTransportMapper.toDto(entity);
    }

    @Override
    public List<DemandeTransportResponse> getAllDemandesTransport() {
        return demandeTransportRepository.findAll()
                .stream()
                .map(DemandeTransportMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public DemandeTransportResponse updateDemandeTransport(String id, DemandeTransportRequest dto) {
        DemandeTransport existing = demandeTransportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DemandeTransport n'existe pas, id: " + id));
        existing.setDateCreation(dto.getDateCreation() != null ? dto.getDateCreation() : existing.getDateCreation());
        existing.setStatut(dto.getStatut() != null ? dto.getStatut() : existing.getStatut());
        existing.setTypeService(dto.getTypeService() != null ? dto.getTypeService() : existing.getTypeService());
        existing.setUserId(dto.getUserId() != null ? dto.getUserId() : existing.getUserId());
        return DemandeTransportMapper.toDto(demandeTransportRepository.save(existing));
    }

    @Override
    public void deleteDemandeTransport(String id) {
        if (!demandeTransportRepository.existsById(id)) {
            throw new RuntimeException("DemandeTransport n'existe pas, id: " + id);
        }
        demandeTransportRepository.deleteById(id);
    }

    @Override // ← @Override fonctionne maintenant car la méthode est dans l'interface
    public List<DemandeTransportResponse> getDemandesByUserId(String userId) {
        return demandeTransportRepository.findByUserId(userId)
                .stream()
                .map(DemandeTransportMapper::toDto)
                .collect(Collectors.toList());
    }
}