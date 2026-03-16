package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.dto.CreneauLivraisonRequest;
import tn.comping.spring.backendcomping.dto.CreneauLivraisonResponse;
import tn.comping.spring.backendcomping.entities.CreneauLivraison;
import tn.comping.spring.backendcomping.repositories.CreneauLivraisonRepository;
import tn.comping.spring.backendcomping.services.CreneauLivraisonService;
import tn.comping.spring.backendcomping.utils.mapper.CreneauLivraisonMapper;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreneauLivraisonServiceImpl implements CreneauLivraisonService {

    private final CreneauLivraisonRepository creneauLivraisonRepository;

    @Override
    public CreneauLivraisonResponse createCreneau(CreneauLivraisonRequest dto) {
        CreneauLivraison entity = CreneauLivraisonMapper.toEntity(dto);
        return CreneauLivraisonMapper.toDto(creneauLivraisonRepository.save(entity));
    }

    @Override
    public CreneauLivraisonResponse getCreneauById(String id) {
        CreneauLivraison entity = creneauLivraisonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CreneauLivraison n'existe pas, id: " + id));
        return CreneauLivraisonMapper.toDto(entity);
    }

    @Override
    public List<CreneauLivraisonResponse> getAllCreneaux() {
        return creneauLivraisonRepository.findAll()
                .stream()
                .map(CreneauLivraisonMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CreneauLivraisonResponse updateCreneau(String id, CreneauLivraisonRequest dto) {
        CreneauLivraison existing = creneauLivraisonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CreneauLivraison n'existe pas, id: " + id));

        existing.setHeureDebut(dto.getHeureDebut() != null ? dto.getHeureDebut() : existing.getHeureDebut());
        existing.setHeureFin(dto.getHeureFin() != null ? dto.getHeureFin() : existing.getHeureFin());
        existing.setDisponible(dto.isDisponible());

        return CreneauLivraisonMapper.toDto(creneauLivraisonRepository.save(existing));
    }

    @Override
    public void deleteCreneau(String id) {
        if (!creneauLivraisonRepository.existsById(id)) {
            throw new RuntimeException("CreneauLivraison n'existe pas, id: " + id);
        }
        creneauLivraisonRepository.deleteById(id);
    }
}