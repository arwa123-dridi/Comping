
package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.entities.*;
import tn.comping.spring.backendcomping.repositories.*;
import tn.comping.spring.backendcomping.utils.mapper.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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

        log.info("Avis créé - ID: {}", avis.getId());

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
    public List<AvisResponseDTO> getAvisByCible(String cibleId, String typeCibleStr) {
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

        return mapToResponseDTO(avisRepository.save(avis));
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
        avis.setMotifRejet(motif);
        avis.setModerateurId(moderateur.getId());

        return mapToResponseDTO(avisRepository.save(avis));
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
                utilisateur.getFirstName() : "Inconnu";

        AvisResponseDTO responseDTO = AvisMapper.toResponseDTO(avis,
                utilisateurNom);

        ReponseAvis reponse =
                reponseAvisRepository.findByAvisId(avis.getId()).orElse(null);
        if (reponse != null) {
            SignupEntity auteurReponse =
                    signupRepository.findById(reponse.getAuteurId()).orElse(null);
            String auteurNom = auteurReponse != null ?
                    auteurReponse.getFirstName() : "Inconnu";

            ReponseAvisDTO reponseDTO =
                    ReponseAvisMapper.toDTO(reponse, auteurNom);
            responseDTO.setReponse(reponseDTO);
        }

        return responseDTO;



    }



}
