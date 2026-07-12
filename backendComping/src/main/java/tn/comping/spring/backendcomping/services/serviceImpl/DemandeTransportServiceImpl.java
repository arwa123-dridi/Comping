package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.comping.spring.backendcomping.dto.DemandeTransportRequest;
import tn.comping.spring.backendcomping.dto.DemandeTransportResponse;
import tn.comping.spring.backendcomping.dto.TraitementDemandeRequest;
import tn.comping.spring.backendcomping.entities.*;
import tn.comping.spring.backendcomping.repositories.CreneauLivraisonRepository;
import tn.comping.spring.backendcomping.repositories.DemandeTransportRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.utils.mapper.DemandeTransportMapper;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DemandeTransportServiceImpl implements DemandeTransportService {

    private static final Map<StatutDemandeTransport, Set<StatutDemandeTransport>> TRANSITIONS = new EnumMap<>(StatutDemandeTransport.class);
    static {
        TRANSITIONS.put(StatutDemandeTransport.EN_ATTENTE, EnumSet.of(StatutDemandeTransport.PLANIFIEE, StatutDemandeTransport.EN_COURS, StatutDemandeTransport.ANNULEE));
        TRANSITIONS.put(StatutDemandeTransport.PLANIFIEE, EnumSet.of(StatutDemandeTransport.EN_COURS, StatutDemandeTransport.ANNULEE));
        TRANSITIONS.put(StatutDemandeTransport.EN_COURS, EnumSet.of(StatutDemandeTransport.LIVREE, StatutDemandeTransport.ANNULEE));
        TRANSITIONS.put(StatutDemandeTransport.LIVREE, EnumSet.noneOf(StatutDemandeTransport.class));
        TRANSITIONS.put(StatutDemandeTransport.ANNULEE, EnumSet.noneOf(StatutDemandeTransport.class));
    }

    private final DemandeTransportRepository demandeTransportRepository;
    private final CreneauLivraisonRepository creneauLivraisonRepository;
    private final SignupRepository signupRepository;
    private final NotificationService notificationService;

    @Override
    public DemandeTransportResponse createDemandeTransport(DemandeTransportRequest dto, String email) {
        SignupEntity user = currentUser(email);
        DemandeTransport entity = DemandeTransportMapper.toEntity(dto, user.getId());
        entity.getHistorique().add(HistoriqueEntry.builder()
                .date(new Date())
                .statutPrecedent(null)
                .statutNouveau(StatutDemandeTransport.EN_ATTENTE.name())
                .commentaire("Demande creee")
                .auteurId(user.getId())
                .build());
        entity = demandeTransportRepository.save(entity);

        notificationService.notifyRole(Role.ORGANISATEUR.name(), NotificationType.NOUVELLE_DEMANDE,
                "Nouvelle demande de transport",
                "Une nouvelle demande de transport (" + entity.getTypeService() + ") a ete soumise",
                RefType.DEMANDE_TRANSPORT, entity.getIdDemandeTransport(), "/admin/logistique/transports");

        return DemandeTransportMapper.toDto(entity);
    }

    @Override
    public DemandeTransportResponse getDemandeTransportById(String id, String email) {
        DemandeTransport entity = findOrThrow(id);
        SignupEntity user = currentUser(email);
        ensureOwnerOrOrganisateur(entity, user);
        return DemandeTransportMapper.toDto(entity);
    }

    @Override
    public List<DemandeTransportResponse> getAllDemandesTransport() {
        return demandeTransportRepository.findAll()
                .stream()
                .map(DemandeTransportMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DemandeTransportResponse> getMesDemandes(String email) {
        SignupEntity user = currentUser(email);
        return demandeTransportRepository.findByUserIdOrderByDateCreationDesc(user.getId())
                .stream()
                .map(DemandeTransportMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public DemandeTransportResponse updateDemandeTransport(String id, DemandeTransportRequest dto, String email) {
        DemandeTransport existing = findOrThrow(id);
        SignupEntity user = currentUser(email);
        ensureOwner(existing, user);

        if (existing.getStatut() != StatutDemandeTransport.EN_ATTENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seule une demande EN_ATTENTE peut etre modifiee");
        }

        DemandeTransportMapper.updateEntityFromDto(existing, dto);
        return DemandeTransportMapper.toDto(demandeTransportRepository.save(existing));
    }

    @Override
    public void deleteDemandeTransport(String id, String email) {
        DemandeTransport existing = findOrThrow(id);
        SignupEntity user = currentUser(email);
        ensureOwner(existing, user);

        if (existing.getStatut() != StatutDemandeTransport.EN_ATTENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seule une demande EN_ATTENTE peut etre annulee/supprimee");
        }
        demandeTransportRepository.deleteById(id);
    }

    @Override
    public DemandeTransportResponse traiterDemande(String id, TraitementDemandeRequest dto, String email) {
        DemandeTransport existing = findOrThrow(id);
        SignupEntity organisateur = currentUser(email);

        StatutDemandeTransport ancienStatut = existing.getStatut();
        StatutDemandeTransport nouveauStatut = dto.getStatut();

        if (nouveauStatut == null || !TRANSITIONS.getOrDefault(ancienStatut, Set.of()).contains(nouveauStatut)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Transition invalide de " + ancienStatut + " vers " + nouveauStatut);
        }

        if (dto.getCreneauLivraisonId() != null && !dto.getCreneauLivraisonId().isBlank()) {
            CreneauLivraison creneau = creneauLivraisonRepository.findById(dto.getCreneauLivraisonId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CreneauLivraison n'existe pas, id: " + dto.getCreneauLivraisonId()));
            creneau.setDisponible(false);
            creneauLivraisonRepository.save(creneau);
            existing.setCreneauLivraisonId(creneau.getIdCreneauLivraison());
        }

        existing.setStatut(nouveauStatut);
        existing.setCommentaireOrganisateur(dto.getCommentaireOrganisateur());
        existing.setDateTraitement(LocalDateTime.now());
        existing.getHistorique().add(HistoriqueEntry.builder()
                .date(new Date())
                .statutPrecedent(ancienStatut.name())
                .statutNouveau(nouveauStatut.name())
                .commentaire(dto.getCommentaireOrganisateur())
                .auteurId(organisateur.getId())
                .build());

        existing = demandeTransportRepository.save(existing);

        notificationService.notifyUser(existing.getUserId(), NotificationType.DEMANDE_TRAITEE,
                "Votre demande de transport a ete traitee",
                "Statut : " + nouveauStatut + (dto.getCommentaireOrganisateur() != null ? " - " + dto.getCommentaireOrganisateur() : ""),
                RefType.DEMANDE_TRANSPORT, existing.getIdDemandeTransport(), "/dashboard/logistique/transports");

        return DemandeTransportMapper.toDto(existing);
    }

    private DemandeTransport findOrThrow(String id) {
        return demandeTransportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DemandeTransport n'existe pas, id: " + id));
    }

    private SignupEntity currentUser(String email) {
        return signupRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouve"));
    }

    private void ensureOwner(DemandeTransport entity, SignupEntity user) {
        if (!entity.getUserId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'etes pas autorise a modifier cette demande");
        }
    }

    private void ensureOwnerOrOrganisateur(DemandeTransport entity, SignupEntity user) {
        boolean isOwner = entity.getUserId().equals(user.getId());
        boolean isOrganisateur = user.getRole() == Role.ORGANISATEUR || user.getRole() == Role.ADMIN;
        if (!isOwner && !isOrganisateur) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'etes pas autorise a consulter cette demande");
        }
    }
}
