package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.comping.spring.backendcomping.dto.AvisRequestDTO;
import tn.comping.spring.backendcomping.dto.AvisResponseDTO;
import tn.comping.spring.backendcomping.dto.StatistiquesAvisDTO;
import tn.comping.spring.backendcomping.entities.Avis;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.entities.StatutAvis;
import tn.comping.spring.backendcomping.entities.TypeCible;
import tn.comping.spring.backendcomping.repositories.AvisRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.utils.mapper.AvisMapper;
import tn.comping.spring.backendcomping.utils.mapper.StatistiquesAvisMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvisServiceImpl implements AvisService {

    private final AvisRepository avisRepository;
    private final SignupRepository signupRepository;

    @Override
    public AvisResponseDTO creerAvis(AvisRequestDTO dto, String utilisateurEmail) {
        SignupEntity utilisateur = signupRepository.findByEmail(utilisateurEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouve"));

        if (dto.getNote() < 1 || dto.getNote() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La note doit etre entre 1 et 5");
        }

        String parentAvisId = dto.getParentAvisId();
        if (parentAvisId != null && !parentAvisId.isEmpty()) {
            avisRepository.findById(parentAvisId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avis parent non trouve"));
        }

        Avis avis = AvisMapper.toEntity(dto, utilisateur.getId());
        if (parentAvisId != null && !parentAvisId.isEmpty()) {
            avis.setParentAvisId(parentAvisId);
        }
        avis = avisRepository.save(avis);

        log.info("Avis cree - ID: {}, Parent: {}", avis.getId(), avis.getParentAvisId());
        return mapToResponseDTO(avis);
    }

    @Override
    public AvisResponseDTO getAvisById(String id) {
        Avis avis = avisRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avis non trouve"));

        AvisResponseDTO dto = mapToResponseDTO(avis);
        dto.setEnfants(buildEnfantsRecursif(id));
        return dto;
    }

    @Override
    public List<AvisResponseDTO> getAvisByCible(String cibleId, String typeCibleStr) {
        TypeCible typeCible = TypeCible.valueOf(typeCibleStr);
        List<Avis> racines = avisRepository.findByCibleIdAndTypeCibleAndValideAndParentAvisIdIsNullOrderByDatePublicationDesc(
                cibleId, typeCible, true);

        return racines.stream()
                .map(this::mapToResponseDTOWithEnfants)
                .collect(Collectors.toList());
    }

    @Override
    public List<AvisResponseDTO> getMesAvis(String utilisateurEmail) {
        SignupEntity utilisateur = signupRepository.findByEmail(utilisateurEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouve"));

        return avisRepository.findByUtilisateurIdOrderByDatePublicationDesc(utilisateur.getId())
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AvisResponseDTO> getAvisByStatut(StatutAvis statut) {
        return avisRepository.findByStatutOrderByDatePublicationDesc(statut)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AvisResponseDTO updateAvis(String id, AvisRequestDTO dto, String utilisateurEmail) {
        Avis avis = avisRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avis non trouve"));

        SignupEntity utilisateur = signupRepository.findByEmail(utilisateurEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouve"));

        if (!avis.getUtilisateurId().equals(utilisateur.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n etes pas autorise a modifier cet avis");
        }

        if (dto.getNote() < 1 || dto.getNote() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La note doit etre entre 1 et 5");
        }

        String newParentId = dto.getParentAvisId();
        if (newParentId != null && !newParentId.isEmpty() && !newParentId.equals(id)) {
            avisRepository.findById(newParentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avis parent non trouve"));
            avis.setParentAvisId(newParentId);
        }

        AvisMapper.updateEntityFromDTO(avis, dto);
        avis = avisRepository.save(avis);

        log.info("Avis mis a jour - ID: {}", avis.getId());
        return mapToResponseDTO(avis);
    }

    @Override
    public void deleteAvis(String id, String utilisateurEmail) {
        Avis avis = avisRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avis non trouve"));

        SignupEntity utilisateur = signupRepository.findByEmail(utilisateurEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouve"));

        if (!avis.getUtilisateurId().equals(utilisateur.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n etes pas autorise a supprimer cet avis");
        }

        deleteEnfantsRecursif(id);
        avisRepository.deleteById(id);

        log.info("Avis supprime - ID: {}", id);
    }

@Override
    public AvisResponseDTO validerAvis(String id, String adminEmail) {
        Avis avis = avisRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avis non trouve"));

        SignupEntity admin = signupRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin non trouvé"));

        avis.setStatut(StatutAvis.VALIDE);
        avis.setValide(true);
        avis.setModerateurId(admin.getId());

        return mapToResponseDTO(avisRepository.save(avis));
    }

@Override
    public AvisResponseDTO rejeterAvis(String id, String motif, String adminEmail) {
        Avis avis = avisRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avis non trouve"));

        SignupEntity admin = signupRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin non trouvé"));

        avis.setStatut(StatutAvis.REJETE);
        avis.setValide(false);
        avis.setMotifRejet(motif);
        avis.setModerateurId(admin.getId());

        return mapToResponseDTO(avisRepository.save(avis));
    }

    @Override
    public StatistiquesAvisDTO getStatistiquesAvis(String cibleId, String typeCibleStr) {
        TypeCible typeCible = TypeCible.valueOf(typeCibleStr);
        List<Avis> avisList = avisRepository.findByCibleIdAndTypeCible(cibleId, typeCible);
        return StatistiquesAvisMapper.toDTO(avisList);
    }

    private AvisResponseDTO mapToResponseDTO(Avis avis) {
        SignupEntity utilisateur = signupRepository.findById(avis.getUtilisateurId()).orElse(null);
        String utilisateurNom = utilisateur != null ? utilisateur.getFirstName() : "Inconnu";
        return AvisMapper.toResponseDTO(avis, utilisateurNom);
    }

    private AvisResponseDTO mapToResponseDTOWithEnfants(Avis avis) {
        AvisResponseDTO dto = mapToResponseDTO(avis);
        dto.setEnfants(buildEnfantsRecursif(avis.getId()));
        return dto;
    }

    private List<AvisResponseDTO> buildEnfantsRecursif(String parentAvisId) {
        List<Avis> enfants = avisRepository.findByParentAvisIdOrderByDatePublicationDesc(parentAvisId);
        if (enfants.isEmpty()) {
            return new ArrayList<>();
        }
        return enfants.stream()
                .map(e -> {
                    AvisResponseDTO dto = mapToResponseDTO(e);
                    dto.setEnfants(buildEnfantsRecursif(e.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private void deleteEnfantsRecursif(String parentAvisId) {
        List<Avis> enfants = avisRepository.findByParentAvisIdOrderByDatePublicationDesc(parentAvisId);
        for (Avis enfant : enfants) {
            deleteEnfantsRecursif(enfant.getId());
            avisRepository.deleteById(enfant.getId());
        }
    }
}
