package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.comping.spring.backendcomping.dto.IncidentRequest;
import tn.comping.spring.backendcomping.dto.IncidentResponse;
import tn.comping.spring.backendcomping.dto.TraitementIncidentRequest;
import tn.comping.spring.backendcomping.entities.*;
import tn.comping.spring.backendcomping.repositories.IncidentRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.utils.mapper.IncidentMapper;

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
public class IncidentServiceImpl implements IncidentService {

    private static final Map<StatutIncident, Set<StatutIncident>> TRANSITIONS = new EnumMap<>(StatutIncident.class);
    static {
        TRANSITIONS.put(StatutIncident.OUVERT, EnumSet.of(StatutIncident.EN_COURS, StatutIncident.RESOLU, StatutIncident.REJETE));
        TRANSITIONS.put(StatutIncident.EN_COURS, EnumSet.of(StatutIncident.RESOLU, StatutIncident.REJETE));
        TRANSITIONS.put(StatutIncident.RESOLU, EnumSet.noneOf(StatutIncident.class));
        TRANSITIONS.put(StatutIncident.REJETE, EnumSet.noneOf(StatutIncident.class));
    }

    private final IncidentRepository incidentRepository;
    private final SignupRepository signupRepository;
    private final NotificationService notificationService;
    private final IncidentPriorityService incidentPriorityService;

    @Override
    public IncidentResponse createIncident(IncidentRequest dto, String email) {
        SignupEntity user = currentUser(email);
        Incident entity = IncidentMapper.toEntity(dto, user.getId());
        entity.setPriorite(incidentPriorityService.classify(entity));
        entity.getHistorique().add(HistoriqueEntry.builder()
                .date(new Date())
                .statutPrecedent(null)
                .statutNouveau(StatutIncident.OUVERT.name())
                .commentaire("Incident signale")
                .auteurId(user.getId())
                .build());
        entity = incidentRepository.save(entity);

        notificationService.notifyRole(Role.ORGANISATEUR.name(), NotificationType.NOUVEL_INCIDENT,
                "Nouvel incident signale",
                "Incident (" + entity.getType() + ") de priorite " + entity.getPriorite(),
                RefType.INCIDENT, entity.getIdIncident(), "/admin/logistique/incidents");

        return IncidentMapper.toDto(entity);
    }

    @Override
    public IncidentResponse getIncidentById(String id, String email) {
        Incident entity = findOrThrow(id);
        SignupEntity user = currentUser(email);
        ensureOwnerOrOrganisateur(entity, user);
        return IncidentMapper.toDto(entity);
    }

    @Override
    public List<IncidentResponse> getAllIncidents() {
        return incidentRepository.findAll()
                .stream()
                .map(IncidentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<IncidentResponse> getMesIncidents(String email) {
        SignupEntity user = currentUser(email);
        return incidentRepository.findByUserIdOrderByDateDeclarationDesc(user.getId())
                .stream()
                .map(IncidentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public IncidentResponse updateIncident(String id, IncidentRequest dto, String email) {
        Incident existing = findOrThrow(id);
        SignupEntity user = currentUser(email);
        ensureOwner(existing, user);

        if (existing.getStatut() != StatutIncident.OUVERT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seul un incident OUVERT peut etre modifie");
        }

        IncidentMapper.updateEntityFromDto(existing, dto);
        existing.setPriorite(incidentPriorityService.classify(existing));
        return IncidentMapper.toDto(incidentRepository.save(existing));
    }

    @Override
    public void deleteIncident(String id, String email) {
        Incident existing = findOrThrow(id);
        SignupEntity user = currentUser(email);
        ensureOwner(existing, user);

        if (existing.getStatut() != StatutIncident.OUVERT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seul un incident OUVERT peut etre supprime");
        }
        incidentRepository.deleteById(id);
    }

    @Override
    public IncidentResponse traiterIncident(String id, TraitementIncidentRequest dto, String email) {
        Incident existing = findOrThrow(id);
        SignupEntity organisateur = currentUser(email);

        StatutIncident ancienStatut = existing.getStatut();
        StatutIncident nouveauStatut = dto.getStatut();

        if (nouveauStatut == null || !TRANSITIONS.getOrDefault(ancienStatut, Set.of()).contains(nouveauStatut)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Transition invalide de " + ancienStatut + " vers " + nouveauStatut);
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

        existing = incidentRepository.save(existing);

        notificationService.notifyUser(existing.getUserId(), NotificationType.INCIDENT_TRAITE,
                "Votre incident a ete traite",
                "Statut : " + nouveauStatut + (dto.getCommentaireOrganisateur() != null ? " - " + dto.getCommentaireOrganisateur() : ""),
                RefType.INCIDENT, existing.getIdIncident(), "/dashboard/logistique/incidents");

        return IncidentMapper.toDto(existing);
    }

    @Override
    public IncidentResponse reclassifier(String id) {
        Incident existing = findOrThrow(id);
        existing.setPriorite(incidentPriorityService.classify(existing));
        return IncidentMapper.toDto(incidentRepository.save(existing));
    }

    private Incident findOrThrow(String id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Incident n'existe pas, id: " + id));
    }

    private SignupEntity currentUser(String email) {
        return signupRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouve"));
    }

    private void ensureOwner(Incident entity, SignupEntity user) {
        if (!entity.getUserId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'etes pas autorise a modifier cet incident");
        }
    }

    private void ensureOwnerOrOrganisateur(Incident entity, SignupEntity user) {
        boolean isOwner = entity.getUserId().equals(user.getId());
        boolean isOrganisateur = user.getRole() == Role.ORGANISATEUR || user.getRole() == Role.ADMIN;
        if (!isOwner && !isOrganisateur) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'etes pas autorise a consulter cet incident");
        }
    }
}
