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
    public AvisResponseDTO creerAvis(AvisRequestDTO dto, String utilisateurEmail) {

        SignupEntity utilisateur = signupRepository.findByEmail(utilisateurEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));

        if (dto.getNote() < 1 || dto.getNote() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La note doit être entre 1 et 5");
        }

        Avis avis = AvisMapper.toEntity(dto, utilisateur.getId());
        avis = avisRepository.save(avis);

        log.info("Avis créé - ID: {}", avis.getId());

        return mapToResponseDTO(avis);
    }

    @Override
    public AvisResponseDTO getAvisById(String id) {
        Avis avis = avisRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avis non trouvé"));

        return mapToResponseDTO(avis);
    }

    @Override
    public List<AvisResponseDTO> getAvisByCible(String cibleId, String typeCibleStr) {
        TypeCible typeCible = TypeCible.valueOf(typeCibleStr);

        return avisRepository.findByCibleIdAndTypeCibleAndValideOrderByDatePublicationDesc(cibleId, typeCible, true)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AvisResponseDTO> getMesAvis(String utilisateurEmail) {

        SignupEntity utilisateur = signupRepository.findByEmail(utilisateurEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));

        return avisRepository.findByUtilisateurIdOrderByDatePublicationDesc(utilisateur.getId())
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAvis(String id, String utilisateurEmail) {

        Avis avis = avisRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avis non trouvé"));

        SignupEntity utilisateur = signupRepository.findByEmail(utilisateurEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));

        if (!avis.getUtilisateurId().equals(utilisateur.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
        }

        reponseAvisRepository.deleteByAvisId(id);
        avisRepository.deleteById(id);
    }

    @Override
    public AvisResponseDTO validerAvis(String id, String moderateurEmail) {

        Avis avis = avisRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avis non trouvé"));

        SignupEntity moderateur = signupRepository.findByEmail(moderateurEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modérateur non trouvé"));

        avis.setStatut(StatutAvis.VALIDE);
        avis.setValide(true);
        avis.setModerateurId(moderateur.getId());

        return mapToResponseDTO(avisRepository.save(avis));
    }

    @Override
    public AvisResponseDTO rejeterAvis(String id, String motif, String moderateurEmail) {

        Avis avis = avisRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avis non trouvé"));

        SignupEntity moderateur = signupRepository.findByEmail(moderateurEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modérateur non trouvé"));

        avis.setStatut(StatutAvis.REJETE);
        avis.setValide(false);
        avis.setMotifRejet(motif);
        avis.setModerateurId(moderateur.getId());

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
        String nom = utilisateur != null ? utilisateur.getFirstName() : "Inconnu";

        return AvisMapper.toResponseDTO(avis, nom);
    }

    @Override
    public List<AvisResponseDTO> getAvisByStatut(StatutAvis statut) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAvisByStatut'");
    }

    @Override
    public AvisResponseDTO updateAvis(String id, AvisRequestDTO dto, String utilisateurEmail) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateAvis'");
    }

    @Override
    public AvisResponseDTO ajouterReponse(String avisId, ReponseAvisRequestDTO dto, String auteurEmail) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'ajouterReponse'");
    }

    @Override
    public void supprimerReponse(String avisId, String auteurEmail) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'supprimerReponse'");
    }
}