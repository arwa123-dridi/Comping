package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.dto.ConventionPartenaireRequest;
import tn.comping.spring.backendcomping.dto.ConventionPartenaireResponse;
import tn.comping.spring.backendcomping.entities.ConventionPartenaire;
import tn.comping.spring.backendcomping.repositories.ConventionPartenaireRepository;
//import tn.comping.spring.backendcomping.services.serviceImpl.ConventionPartenaireService;
import tn.comping.spring.backendcomping.utils.mapper.ConventionPartenaireMapper;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConventionPartenaireServiceImpl implements ConventionPartenaireService {

    private final ConventionPartenaireRepository repository;

    @Override
    public ConventionPartenaireResponse createConvention(ConventionPartenaireRequest dto) {
        ConventionPartenaire entity = ConventionPartenaireMapper.toEntity(dto);
        return ConventionPartenaireMapper.toDto(repository.save(entity));
    }

    @Override
    public ConventionPartenaireResponse getConventionById(String id) {
        ConventionPartenaire entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Convention non trouvée, id: " + id));
        return ConventionPartenaireMapper.toDto(entity);
    }

    @Override
    public List<ConventionPartenaireResponse> getAllConventions() {
        return repository.findAll()
                .stream()
                .map(ConventionPartenaireMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ConventionPartenaireResponse updateConvention(String id, ConventionPartenaireRequest dto) {
        ConventionPartenaire existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Convention non trouvée, id: " + id));

        existing.setDateDebut(dto.getDateDebut() != null ? dto.getDateDebut() : existing.getDateDebut());
        existing.setDateFin(dto.getDateFin() != null ? dto.getDateFin() : existing.getDateFin());
        existing.setRemise(dto.getRemise() != 0 ? dto.getRemise() : existing.getRemise());
        existing.setConditions(dto.getConditions() != null ? dto.getConditions() : existing.getConditions());

        return ConventionPartenaireMapper.toDto(repository.save(existing));
    }

    @Override
    public void deleteConvention(String id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Convention non trouvée, id: " + id);
        }
        repository.deleteById(id);
    }
}