package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.entities.SiteCamping;
import tn.comping.spring.backendcomping.repositories.SiteCampingRepository;
import tn.comping.spring.backendcomping.utils.mapper.SiteCampingMapper;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SiteCampingServiceImpl implements SiteCampingService {

    private final SiteCampingRepository repository;
    private final SiteCampingMapper mapper;

    @Override
    public List<SiteCampingResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public SiteCampingResponse getById(String id) {
        return repository.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Site non trouvé : " + id));
    }

    @Override
    public SiteCampingResponse create(SiteCampingRequest request) {
        SiteCamping site = mapper.toEntity(request);
        return mapper.toResponse(repository.save(site));
    }

    @Override
    public SiteCampingResponse update(String id, SiteCampingRequest request) {
        SiteCamping site = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Site non trouvé : " + id));
        site.setNom(request.getNom());
        site.setDescription(request.getDescription());
        site.setLocalisation(request.getLocalisation());
        site.setLatitude(request.getLatitude());
        site.setLongitude(request.getLongitude());
        site.setCapacite(request.getCapacite());
        site.setTarifs(request.getTarifs());
        site.setDisponible(request.isDisponible());
        site.setConsignesSecurite(request.getConsignesSecurite());
        site.setPhotos(request.getPhotos());
        return mapper.toResponse(repository.save(site));
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public List<SiteCampingResponse> getDisponibles() {
        return repository.findByDisponibleTrue().stream()
                .map(mapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<SiteCampingResponse> getByLocalisation(String localisation) {
        return repository.findByLocalisation(localisation).stream()
                .map(mapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<SiteCampingResponse> getByProprietaire(String proprietaireId) {
        return repository.findByProprietaireId(proprietaireId).stream()
                .map(mapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<SiteCampingResponse> filtrer(Double prixMin, Double prixMax, Boolean disponible) {
        return repository.findAll().stream()
                .filter(s -> prixMin == null || s.getTarifs() >= prixMin)
                .filter(s -> prixMax == null || s.getTarifs() <= prixMax)
                .filter(s -> disponible == null || s.isDisponible() == disponible)
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
}