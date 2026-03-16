package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.entities.Avis;
import tn.comping.spring.backendcomping.repositories.AvisRepository;
import tn.comping.spring.backendcomping.repositories.SiteCampingRepository;
import tn.comping.spring.backendcomping.entities.SiteCamping;
import tn.comping.spring.backendcomping.utils.mapper.AvisMapper;
import java.util.Date;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvisServiceImpl implements AvisService {

    private final AvisRepository avisRepository;
    private final SiteCampingRepository siteCampingRepository;
    private final AvisMapper mapper;

    @Override
    public AvisResponse ajouterAvis(AvisRequest request) {
        if (request.getNote() < 1 || request.getNote() > 5)
            throw new RuntimeException("La note doit être entre 1 et 5");

        Avis avis = mapper.toEntity(request);
        avis.setDateCreation(new Date());
        avis.setStatutModeration("PUBLIE");
        AvisResponse response = mapper.toResponse(avisRepository.save(avis));

        // Recalculer la note moyenne du site
        recalculerNoteMoyenne(request.getSiteCampingId());
        return response;
    }

    @Override
    public List<AvisResponse> getAvisBySite(String siteCampingId) {
        return avisRepository.findBySiteCampingIdAndStatutModeration(siteCampingId, "PUBLIE")
                .stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<AvisResponse> getAvisByUtilisateur(String utilisateurId) {
        return avisRepository.findByUtilisateurId(utilisateurId)
                .stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public AvisResponse modererAvis(String id, String statut) {
        Avis avis = avisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Avis non trouvé : " + id));
        avis.setStatutModeration(statut);
        return mapper.toResponse(avisRepository.save(avis));
    }

    @Override
    public void supprimerAvis(String id) {
        avisRepository.deleteById(id);
    }

    private void recalculerNoteMoyenne(String siteCampingId) {
        List<Avis> avisList = avisRepository.findBySiteCampingId(siteCampingId);
        OptionalDouble moyenne = avisList.stream()
                .mapToInt(Avis::getNote).average();
        siteCampingRepository.findById(siteCampingId).ifPresent(site -> {
            site.setNoteMoyenne(moyenne.orElse(0.0));
            siteCampingRepository.save(site);
        });
    }
}