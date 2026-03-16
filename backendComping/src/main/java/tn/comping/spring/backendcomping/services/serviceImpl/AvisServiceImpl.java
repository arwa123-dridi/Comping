package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
<<<<<<< HEAD
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
=======
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ResponseStatusException;
import tn.comping.spring.backendcomping.dto.AvisRequestDTO;
import tn.comping.spring.backendcomping.dto.AvisResponseDTO;
import tn.comping.spring.backendcomping.dto.ReponseAvisDTO;
import tn.comping.spring.backendcomping.dto.ReponseAvisRequestDTO;
import tn.comping.spring.backendcomping.dto.StatistiquesAvisDTO;
import tn.comping.spring.backendcomping.entities.Avis;
import tn.comping.spring.backendcomping.entities.ReponseAvis;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.entities.StatutAvis;
import tn.comping.spring.backendcomping.entities.TypeCible;
import tn.comping.spring.backendcomping.repositories.AvisRepository;
import tn.comping.spring.backendcomping.repositories.ReponseAvisRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.services.serviceImpl.AvisService;
import tn.comping.spring.backendcomping.utils.mapper.AvisMapper;
import tn.comping.spring.backendcomping.utils.mapper.ReponseAvisMapper;
import tn.comping.spring.backendcomping.utils.mapper.StatistiquesAvisMapper;

import java.util.List;
>>>>>>> origin/mariem-sellami
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
<<<<<<< HEAD
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
=======
@Slf4j
public class AvisServiceImpl implements AvisService {

    private final AvisRepository avisRepository;
    private final ReponseAvisRepository reponseAvisRepository;
    private final SignupRepository signupRepository;

    @Override
    public AvisResponseDTO creerAvis(AvisRequestDTO dto, String
utilisateurEmail) {
        SignupEntity utilisateur =
signupRepository.findByEmail(utilisateurEmail)
                .orElseThrow(() -> new
ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouve"));

        if (dto.getNote() < 1 || dto.getNote() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
"La note doit etre entre 1 et 5");
        }

        Avis avis = AvisMapper.toEntity(dto, utilisateur.getId());
        avis = avisRepository.save(avis);

        log.info("Avis cree avec succes - ID: {}", avis.getId());

        return mapToResponseDTO(avis);
    }

    @Override
    public AvisResponseDTO getAvisById(String id) {
        Avis avis = avisRepository.findById(id)
                .orElseThrow(() -> new
ResponseStatusException(HttpStatus.NOT_FOUND, "Avis non trouve"));

        return mapToResponseDTO(avis);
    }

    @Override
    public List<AvisResponseDTO> getAvisByCible(String cibleId, String
typeCibleStr) {
        TypeCible typeCible = TypeCible.valueOf(typeCibleStr);

        return avisRepository.findByCibleIdAndTypeCibleAndValideOrderByDatePublicationDesc(cibleId,
typeCible, true)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AvisResponseDTO> getMesAvis(String utilisateurEmail) {
        SignupEntity utilisateur =
signupRepository.findByEmail(utilisateurEmail)
                .orElseThrow(() -> new
ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouve"));

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
    public AvisResponseDTO updateAvis(String id, AvisRequestDTO dto,
String utilisateurEmail) {
        Avis avis = avisRepository.findById(id)
                .orElseThrow(() -> new
ResponseStatusException(HttpStatus.NOT_FOUND, "Avis non trouve"));

        SignupEntity utilisateur =
signupRepository.findByEmail(utilisateurEmail)
                .orElseThrow(() -> new
ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouve"));

        if (!avis.getUtilisateurId().equals(utilisateur.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
"Vous n etes pas autorise a modifier cet avis");
        }

        if (dto.getNote() < 1 || dto.getNote() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
"La note doit etre entre 1 et 5");
        }

        AvisMapper.updateEntityFromDTO(avis, dto);
        avis = avisRepository.save(avis);

        log.info("Avis mis a jour - ID: {}", avis.getId());

        return mapToResponseDTO(avis);
    }

    @Override
    public void deleteAvis(String id, String utilisateurEmail) {
        Avis avis = avisRepository.findById(id)
                .orElseThrow(() -> new
ResponseStatusException(HttpStatus.NOT_FOUND, "Avis non trouve"));

        SignupEntity utilisateur =
signupRepository.findByEmail(utilisateurEmail)
                .orElseThrow(() -> new
ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouve"));

        if (!avis.getUtilisateurId().equals(utilisateur.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
"Vous n etes pas autorise a supprimer cet avis");
        }

        reponseAvisRepository.deleteByAvisId(id);
        avisRepository.deleteById(id);

        log.info("Avis supprime - ID: {}", id);
    }

    @Override
    public AvisResponseDTO validerAvis(String id, String moderateurEmail) {
        Avis avis = avisRepository.findById(id)
                .orElseThrow(() -> new
ResponseStatusException(HttpStatus.NOT_FOUND, "Avis non trouve"));

        SignupEntity moderateur = signupRepository.findByEmail(moderateurEmail)
                .orElseThrow(() -> new
ResponseStatusException(HttpStatus.NOT_FOUND, "Moderateur non trouve"));

        avis.setStatut(StatutAvis.VALIDE);
        avis.setValide(true);
        avis.setModerateurId(moderateur.getId());

        avis = avisRepository.save(avis);

        log.info("Avis valide - ID: {}", id);

        return mapToResponseDTO(avis);
    }

    @Override
    public AvisResponseDTO rejeterAvis(String id, String motif, String
moderateurEmail) {
        Avis avis = avisRepository.findById(id)
                .orElseThrow(() -> new
ResponseStatusException(HttpStatus.NOT_FOUND, "Avis non trouve"));

        SignupEntity moderateur = signupRepository.findByEmail(moderateurEmail)
                .orElseThrow(() -> new
ResponseStatusException(HttpStatus.NOT_FOUND, "Moderateur non trouve"));

        avis.setStatut(StatutAvis.REJETE);
        avis.setValide(false);
        avis.setModerateurId(moderateur.getId());
        avis.setMotifRejet(motif);

        avis = avisRepository.save(avis);

        log.info("Avis rejete - ID: {}, Motif: {}", id, motif);

        return mapToResponseDTO(avis);
    }

    @Override
    public AvisResponseDTO ajouterReponse(String avisId,
ReponseAvisRequestDTO dto, String auteurEmail) {
        Avis avis = avisRepository.findById(avisId)
                .orElseThrow(() -> new
ResponseStatusException(HttpStatus.NOT_FOUND, "Avis non trouve"));

        SignupEntity auteur = signupRepository.findByEmail(auteurEmail)
                .orElseThrow(() -> new
ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouve"));

        if (reponseAvisRepository.findByAvisId(avisId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
"Une reponse existe deja pour cet avis");
        }

        ReponseAvis reponse = ReponseAvisMapper.toEntity(dto, avisId,
auteur.getId(), auteur.getRole().name());

        reponseAvisRepository.save(reponse);

        log.info("Reponse ajoutee a l avis - ID Avis: {}", avisId);

        return mapToResponseDTO(avis);
    }

    @Override
    public void supprimerReponse(String avisId, String auteurEmail) {
        ReponseAvis reponse = reponseAvisRepository.findByAvisId(avisId)
                .orElseThrow(() -> new
ResponseStatusException(HttpStatus.NOT_FOUND, "Reponse non trouvee"));

        SignupEntity auteur = signupRepository.findByEmail(auteurEmail)
                .orElseThrow(() -> new
ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouve"));

        if (!reponse.getAuteurId().equals(auteur.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
"Vous n etes pas autorise a supprimer cette reponse");
        }

        reponseAvisRepository.deleteById(reponse.getId());

        log.info("Reponse supprimee - ID Avis: {}", avisId);

    }

    @Override
    public StatistiquesAvisDTO getStatistiquesAvis(String cibleId,
String typeCibleStr) {
        TypeCible typeCible = TypeCible.valueOf(typeCibleStr);

        List<Avis> avisList =
avisRepository.findByCibleIdAndTypeCible(cibleId, typeCible);

        return StatistiquesAvisMapper.toDTO(avisList);
    }

    private AvisResponseDTO mapToResponseDTO(Avis avis) {
        SignupEntity utilisateur =
signupRepository.findById(avis.getUtilisateurId()).orElse(null);
        String utilisateurNom = utilisateur != null ?
utilisateur.getName() : "Inconnu";

        AvisResponseDTO responseDTO = AvisMapper.toResponseDTO(avis,
utilisateurNom);

        ReponseAvis reponse =
reponseAvisRepository.findByAvisId(avis.getId()).orElse(null);
        if (reponse != null) {
            SignupEntity auteurReponse =
signupRepository.findById(reponse.getAuteurId()).orElse(null);
            String auteurNom = auteurReponse != null ?
auteurReponse.getName() : "Inconnu";

            ReponseAvisDTO reponseDTO =
ReponseAvisMapper.toDTO(reponse, auteurNom);
            responseDTO.setReponse(reponseDTO);
        }

        return responseDTO;
>>>>>>> origin/mariem-sellami
    }
}