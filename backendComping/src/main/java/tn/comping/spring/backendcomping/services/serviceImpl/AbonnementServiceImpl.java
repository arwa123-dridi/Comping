package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.comping.spring.backendcomping.dto.AbonnementResponseDTO;
import tn.comping.spring.backendcomping.entities.Abonnement;
import tn.comping.spring.backendcomping.repositories.AbonnementRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.services.AbonnementService;
import tn.comping.spring.backendcomping.utils.mapper.AbonnementMapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AbonnementServiceImpl implements AbonnementService {

    private final AbonnementRepository abonnementRepository;
    private final SignupRepository signupRepository;

    @Override
    public AbonnementResponseDTO suivre(String suiviId, String currentUserEmail) {
        String suiveurId = signupRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"))
            .getId();

        // Règle métier 2 : pas d'auto-suivi
        if (suiveurId.equals(suiviId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible de se suivre soi-même");
        }

        // Vérifier existence
        if (abonnementRepository.existsBySuiveurIdAndSuiviId(suiveurId, suiviId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Déjà abonné");
        }

        Abonnement abonnement = Abonnement.builder()
            .suiveurId(suiveurId)
            .suiviId(suiviId)
            .dateAbonnement(new Date())
            .build();

        abonnement = abonnementRepository.save(abonnement);
        log.info("Abonnement créé {} → {}", suiveurId, suiviId);
        
        return AbonnementMapper.toResponseDTO(abonnement);
    }

    @Override
    public void nePlusSuivre(String suiviId, String currentUserEmail) {
        String suiveurId = signupRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"))
            .getId();

        Abonnement abonnement = abonnementRepository.findBySuiveurIdAndSuiviId(suiveurId, suiviId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Abonnement non trouvé"));

        abonnementRepository.delete(abonnement);
        log.info("Abonnement supprimé {} → {}", suiveurId, suiviId);
    }

    @Override
    public List<AbonnementResponseDTO> getMesAbonnements(String currentUserEmail) {
        String suiveurId = signupRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"))
            .getId();

        List<Abonnement> abonnements = abonnementRepository.findBySuiveurIdOrderByDateAbonnementDesc(suiveurId);
        return AbonnementMapper.toResponseDTOList(abonnements);
    }

    @Override
    public List<AbonnementResponseDTO> getMesAbonnes(String currentUserEmail) {
        String suiviId = signupRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"))
            .getId();

        List<Abonnement> abonnes = abonnementRepository.findBySuiviIdOrderByDateAbonnementDesc(suiviId);
        return AbonnementMapper.toResponseDTOList(abonnes);
    }

    @Override
    public Object getStats(String userId) {
        long abonnes = abonnementRepository.countBySuiviId(userId);
        long abonnements = abonnementRepository.countBySuiveurId(userId);
        
        return Map.of(
            "abonnes", abonnes,
            "abonnements", abonnements
        );
    }
}

