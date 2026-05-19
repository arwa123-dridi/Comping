package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.comping.spring.backendcomping.dto.AbonnementResponseDTO;
import tn.comping.spring.backendcomping.entities.Abonnement;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.repositories.AbonnementRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.services.AbonnementService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
public class AbonnementServiceImpl implements AbonnementService {

    private final AbonnementRepository abonnementRepository;
    private final SignupRepository signupRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    @Override
    public AbonnementResponseDTO suivre(String suiveurId, String suiviId) {
        if (suiveurId.equals(suiviId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous ne pouvez pas vous suivre vous-même.");
        }
        if (abonnementRepository.existsBySuiveurIdAndSuiviId(suiveurId, suiviId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vous suivez déjà ce campeur.");
        }
        Abonnement abonnement = Abonnement.builder()
                .suiveurId(suiveurId)
                .suiviId(suiviId)
                .build();
        Abonnement saved = abonnementRepository.save(abonnement);

        String suiveurNom = signupRepository.findById(suiveurId)
                .map(u -> (u.getFirstName() + " " + u.getLastName()).trim())
                .filter(n -> !n.isBlank())
                .orElse("Quelqu'un");
                
        // Create persistent notification
        notificationService.createNotification(suiviId, suiveurId, "FOLLOW", suiveurId, suiveurNom + " a commencé à vous suivre.");

        // Real-time notification
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "NEW_FOLLOWER");
        payload.put("expediteurNom", suiveurNom);
        messagingTemplate.convertAndSend("/topic/user/" + suiviId + "/notifications", payload);

        return toDTO(saved);
    }

    @Override
    public void retirer(String suiveurId, String suiviId) {
        if (!abonnementRepository.existsBySuiveurIdAndSuiviId(suiveurId, suiviId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Abonnement introuvable.");
        }
        abonnementRepository.deleteBySuiveurIdAndSuiviId(suiveurId, suiviId);
    }

    @Override
    public List<AbonnementResponseDTO> getMesAbonnements(String suiveurId) {
        return abonnementRepository.findBySuiveurId(suiveurId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean estSuivi(String suiveurId, String suiviId) {
        return abonnementRepository.existsBySuiveurIdAndSuiviId(suiveurId, suiviId);
    }

    @Override
    public Map<String, Long> getFollowStats(String userId) {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("followers", abonnementRepository.countBySuiviId(userId));
        stats.put("following", abonnementRepository.countBySuiveurId(userId));
        return stats;
    }

    private AbonnementResponseDTO toDTO(Abonnement a) {
        SignupEntity user = signupRepository.findById(a.getSuiviId())
                .or(() -> signupRepository.findByEmail(a.getSuiviId()))
                .orElse(null);
        String nom = user != null ? (user.getFirstName() + " " + user.getLastName()).trim() : "Campeur";
        String email = user != null ? user.getEmail() : a.getSuiviId();
        return AbonnementResponseDTO.builder()
                .id(a.getId())
                .suiviId(a.getSuiviId())
                .suiviNom(nom.isEmpty() ? "Campeur" : nom)
                .suiviEmail(email)
                .build();
    }
}
