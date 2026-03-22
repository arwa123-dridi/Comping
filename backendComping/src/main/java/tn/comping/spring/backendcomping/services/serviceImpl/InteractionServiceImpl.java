package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.comping.spring.backendcomping.dto.InteractionRequestDTO;
import tn.comping.spring.backendcomping.dto.InteractionResponseDTO;
import tn.comping.spring.backendcomping.entities.CibleType;
import tn.comping.spring.backendcomping.entities.Interaction;
import tn.comping.spring.backendcomping.entities.TypeInteraction;
import tn.comping.spring.backendcomping.repositories.InteractionRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.services.InteractionService;
import tn.comping.spring.backendcomping.utils.mapper.InteractionMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InteractionServiceImpl implements InteractionService {

    private final InteractionRepository interactionRepository;
    private final SignupRepository signupRepository;

    @Override
    public InteractionResponseDTO creerLike(InteractionRequestDTO dto, String utilisateurEmail) {
        return creerInteraction(dto, utilisateurEmail, TypeInteraction.LIKE);
    }

    @Override
    public InteractionResponseDTO creerCommentaire(InteractionRequestDTO dto, String utilisateurEmail) {
        if (dto.getContenu() == null || dto.getContenu().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contenu du commentaire obligatoire");
        }
        if (dto.getContenu().length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Commentaire trop long (max 1000 caractères)");
        }
        return creerInteraction(dto, utilisateurEmail, TypeInteraction.COMMENTAIRE);
    }

    private InteractionResponseDTO creerInteraction(InteractionRequestDTO dto, String utilisateurEmail, TypeInteraction type) {
        // Vérifier unicité (règle métier 1)
        interactionRepository.findByUtilisateurIdAndCibleTypeAndCibleId(utilisateurEmail, 
            dto.getCibleType(), dto.getCibleId())
            .ifPresent(existing -> {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Interaction de type " + type + " existe déjà");
            });

        Interaction interaction = InteractionMapper.toEntity(dto, utilisateurEmail, utilisateurEmail, type);
        interaction = interactionRepository.save(interaction);
        
        log.info("Interaction créée: {} sur {} {}", type, dto.getCibleType(), dto.getCibleId());
        return InteractionMapper.toResponseDTO(interaction);
    }

    @Override
    public List<InteractionResponseDTO> getLikes(CibleType cibleType, String cibleId) {
        List<Interaction> likes = interactionRepository
            .findByCibleTypeAndCibleIdAndTypeAndVisible(cibleType, cibleId, TypeInteraction.LIKE, true);
        return InteractionMapper.toResponseDTOList(likes);
    }

    @Override
    public List<InteractionResponseDTO> getCommentaires(CibleType cibleType, String cibleId) {
        List<Interaction> commentaires = interactionRepository
            .findByCibleTypeAndCibleIdAndTypeAndVisibleOrderByDateInteractionDesc(cibleType, cibleId, 
                TypeInteraction.COMMENTAIRE, true);
        return InteractionMapper.toResponseDTOList(commentaires);
    }

    @Override
    public void supprimerLike(String interactionId, String utilisateurEmail) {
        supprimerInteraction(interactionId, utilisateurEmail, TypeInteraction.LIKE);
    }

    @Override
    public void supprimerCommentaire(String interactionId, String utilisateurEmail) {
        supprimerInteraction(interactionId, utilisateurEmail, TypeInteraction.COMMENTAIRE);
    }

    private void supprimerInteraction(String interactionId, String utilisateurEmail, TypeInteraction type) {
        Interaction interaction = interactionRepository.findById(interactionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interaction non trouvée"));

        if (!interaction.getUtilisateurId().equals(utilisateurEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé à supprimer cette interaction");
        }

        if (interaction.getType() != type) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type d'interaction incorrect");
        }

interaction.setVisible(false);
        interactionRepository.save(interaction);
        
        log.info("Interaction supprimée (soft): {} par {}", interactionId, utilisateurEmail);
    }

    @Override
    public long compterLikes(CibleType cibleType, String cibleId) {
        return interactionRepository.countByCibleTypeAndCibleIdAndTypeAndVisible(
            cibleType, cibleId, TypeInteraction.LIKE, true);
    }
}

