package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.comping.spring.backendcomping.dto.AvisRequestDTO;
import tn.comping.spring.backendcomping.dto.AvisResponseDTO;
import tn.comping.spring.backendcomping.dto.StatistiquesAvisDTO;
import tn.comping.spring.backendcomping.entities.Avis;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.entities.StatutAvis;
import tn.comping.spring.backendcomping.entities.TypeCible;
import tn.comping.spring.backendcomping.entities.Abonnement;
import tn.comping.spring.backendcomping.entities.Role;
import tn.comping.spring.backendcomping.repositories.AbonnementRepository;
import tn.comping.spring.backendcomping.repositories.AvisRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.utils.mapper.AvisMapper;
import tn.comping.spring.backendcomping.utils.mapper.StatistiquesAvisMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvisServiceImpl implements AvisService {

    private final AvisRepository avisRepository;
    private final SignupRepository signupRepository;
    private final AbonnementRepository abonnementRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public AvisResponseDTO creerAvis(AvisRequestDTO dto, String utilisateurEmail) {
        validateAvisPayload(dto);
        SignupEntity utilisateur = signupRepository.findByEmail(utilisateurEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouve"));

        String parentAvisId = dto.getParentAvisId();
        if (parentAvisId != null && !parentAvisId.isEmpty()) {
            Avis parent = avisRepository.findById(parentAvisId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avis parent non trouve"));
            if (!parent.getCibleId().equals(dto.getCibleId()) || parent.getTypeCible() != dto.getTypeCible()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La reponse doit concerner la meme cible que l'avis parent");
            }
        }

        Avis avis = AvisMapper.toEntity(dto, utilisateur.getId());
        if (parentAvisId != null && !parentAvisId.isEmpty()) {
            avis.setParentAvisId(parentAvisId);
        }
        final Avis savedAvis = avisRepository.save(avis);
        log.info("Avis cree - ID: {}, Parent: {}", savedAvis.getId(), savedAvis.getParentAvisId());

        // Notifier tous les admins qu'un nouvel avis a été déposé
        final String auteurNom = (utilisateur.getFirstName() + " " + utilisateur.getLastName()).trim();
        final String expediteurNom = auteurNom.isEmpty() ? utilisateurEmail : auteurNom;
        signupRepository.findByRole(Role.ADMIN).forEach(admin -> {
            if (admin.getEmail() != null) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("type", "NEW_AVIS");
                payload.put("expediteurNom", expediteurNom);
                payload.put("avisId", savedAvis.getId());
                messagingTemplate.convertAndSend("/topic/user/" + admin.getEmail() + "/notifications", payload);
            }
        });

        return mapToResponseDTO(savedAvis);
    }

    @Override
    public AvisResponseDTO getAvisById(String id) {
        Avis avis = avisRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avis non trouve"));

        AvisResponseDTO dto = mapToResponseDTO(avis);
        dto.setEnfants(buildEnfantsRecursif(id, true));
        return dto;
    }

    @Override
    public List<AvisResponseDTO> getAvisByCible(String cibleId, String typeCibleStr) {
        TypeCible typeCible = parseTypeCible(typeCibleStr);
        List<Avis> racines = avisRepository.findByCibleIdAndTypeCibleAndValideAndParentAvisIdIsNullOrderByDatePublicationDesc(
                cibleId, typeCible, true);

        return racines.stream()
                .map(this::mapToResponseDTOWithEnfantsValides)
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
        validateAvisPayload(dto);
        Avis avis = avisRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avis non trouve"));

        SignupEntity utilisateur = signupRepository.findByEmail(utilisateurEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouve"));

        if (!avis.getUtilisateurId().equals(utilisateur.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n etes pas autorise a modifier cet avis");
        }

        String newParentId = dto.getParentAvisId();
        if (newParentId != null && !newParentId.isEmpty()) {
            if (newParentId.equals(id) || isDescendantOf(newParentId, id)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un avis ne peut pas devenir son propre parent");
            }
            Avis parent = avisRepository.findById(newParentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avis parent non trouve"));
            if (!parent.getCibleId().equals(dto.getCibleId()) || parent.getTypeCible() != dto.getTypeCible()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La reponse doit concerner la meme cible que l'avis parent");
            }
            avis.setParentAvisId(newParentId);
        } else {
            avis.setParentAvisId(null);
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
        avis.setAdminId(admin.getId());
        AvisResponseDTO result = mapToResponseDTO(avisRepository.save(avis));

        // Notifier l'auteur de l'avis
        SignupEntity auteur = signupRepository.findById(avis.getUtilisateurId()).orElse(null);
        if (auteur != null && auteur.getEmail() != null) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "AVIS_VALIDE");
            payload.put("expediteurNom", "Campino");
            payload.put("avisId", avis.getId());
            messagingTemplate.convertAndSend("/topic/user/" + auteur.getEmail() + "/notifications", payload);
        }

        return result;
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
        avis.setAdminId(admin.getId());
        AvisResponseDTO result = mapToResponseDTO(avisRepository.save(avis));

        // Notifier l'auteur de l'avis
        SignupEntity auteur = signupRepository.findById(avis.getUtilisateurId()).orElse(null);
        if (auteur != null && auteur.getEmail() != null) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "AVIS_REJETE");
            payload.put("expediteurNom", "Campino");
            payload.put("avisId", avis.getId());
            payload.put("motif", motif);
            messagingTemplate.convertAndSend("/topic/user/" + auteur.getEmail() + "/notifications", payload);
        }

        return result;
    }

    @Override
    public List<AvisResponseDTO> getAvisValides() {
        return avisRepository.findByStatutAndParentAvisIdIsNullOrderByDatePublicationDesc(StatutAvis.VALIDE)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AvisResponseDTO> getAvisAmis(String utilisateurEmail) {
        List<String> suiviEmails = abonnementRepository.findBySuiveurId(utilisateurEmail)
                .stream().map(Abonnement::getSuiviId).collect(Collectors.toList());
        if (suiviEmails.isEmpty()) return List.of();

        List<String> suiviIds = suiviEmails.stream()
                .map(email -> signupRepository.findByEmail(email)
                        .map(SignupEntity::getId).orElse(null))
                .filter(id -> id != null)
                .collect(Collectors.toList());
        if (suiviIds.isEmpty()) return List.of();

        return avisRepository.findByUtilisateurIdInAndValideOrderByDatePublicationDesc(suiviIds, true)
                .stream()
                .filter(a -> a.getParentAvisId() == null)
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public StatistiquesAvisDTO getStatistiquesAvis(String cibleId, String typeCibleStr) {
        TypeCible typeCible = parseTypeCible(typeCibleStr);
        List<Avis> avisList = avisRepository.findByCibleIdAndTypeCible(cibleId, typeCible)
                .stream()
                .filter(Avis::isValide)
                .collect(Collectors.toList());
        return StatistiquesAvisMapper.toDTO(avisList);
    }

    private void validateAvisPayload(AvisRequestDTO dto) {
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'avis est obligatoire");
        }
        if (dto.getNote() < 1 || dto.getNote() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La note doit etre entre 1 et 5");
        }
        if (dto.getTypeCible() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le type de cible est obligatoire");
        }
        if (dto.getCibleId() == null || dto.getCibleId().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cible est obligatoire");
        }
        if (dto.getCommentaire() == null || dto.getCommentaire().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le commentaire est obligatoire");
        }
    }

    private TypeCible parseTypeCible(String typeCibleStr) {
        if (typeCibleStr == null || typeCibleStr.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le type de cible est obligatoire");
        }
        try {
            return TypeCible.valueOf(typeCibleStr.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type de cible invalide");
        }
    }

    private AvisResponseDTO mapToResponseDTO(Avis avis) {
        SignupEntity utilisateur = signupRepository.findById(avis.getUtilisateurId()).orElse(null);
        String utilisateurNom = utilisateur != null ? utilisateur.getFirstName() : "Inconnu";
        return AvisMapper.toResponseDTO(avis, utilisateurNom);
    }

    private AvisResponseDTO mapToResponseDTOWithEnfantsValides(Avis avis) {
        AvisResponseDTO dto = mapToResponseDTO(avis);
        dto.setEnfants(buildEnfantsRecursif(avis.getId(), false));
        return dto;
    }

    private List<AvisResponseDTO> buildEnfantsRecursif(String parentAvisId, boolean includeNonValides) {
        List<Avis> enfants = avisRepository.findByParentAvisIdOrderByDatePublicationDesc(parentAvisId);
        if (enfants.isEmpty()) {
            return new ArrayList<>();
        }
        return enfants.stream()
                .filter(e -> includeNonValides || e.isValide())
                .map(e -> {
                    AvisResponseDTO dto = mapToResponseDTO(e);
                    dto.setEnfants(buildEnfantsRecursif(e.getId(), includeNonValides));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private boolean isDescendantOf(String possibleChildId, String parentId) {
        List<Avis> enfants = avisRepository.findByParentAvisIdOrderByDatePublicationDesc(parentId);
        for (Avis enfant : enfants) {
            if (enfant.getId().equals(possibleChildId) || isDescendantOf(possibleChildId, enfant.getId())) {
                return true;
            }
        }
        return false;
    }

    private void deleteEnfantsRecursif(String parentAvisId) {
        List<Avis> enfants = avisRepository.findByParentAvisIdOrderByDatePublicationDesc(parentAvisId);
        for (Avis enfant : enfants) {
            deleteEnfantsRecursif(enfant.getId());
            avisRepository.deleteById(enfant.getId());
        }
    }
}
