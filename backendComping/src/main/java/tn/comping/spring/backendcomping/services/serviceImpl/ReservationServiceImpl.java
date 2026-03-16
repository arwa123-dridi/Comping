package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.entities.*;
import tn.comping.spring.backendcomping.repositories.*;
import tn.comping.spring.backendcomping.utils.mapper.ReservationMapper;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository repository;
    private final SiteCampingRepository siteCampingRepository;
    private final ReservationMapper mapper;
    private final EmailService emailService;

    @Override
    public List<ReservationResponse> getAllReservations() {
        return repository.findAll().stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public ReservationResponse getReservationById(String id) {
        return repository.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée : " + id));
    }

    @Override
public ReservationResponse createReservation(ReservationRequest request) {
    if (request.getSiteCampingId() == null || request.getSiteCampingId().isBlank())
        throw new RuntimeException("siteCampingId est obligatoire");

    SiteCamping site = siteCampingRepository.findById(request.getSiteCampingId())
            .orElseThrow(() -> new RuntimeException("Site non trouvé : " + request.getSiteCampingId()));

    if (!site.isDisponible())
        throw new RuntimeException("Site non disponible");

    Reservation reservation = mapper.toEntity(request);
    reservation.setStatut(StatutReservation.EN_ATTENTE);
    Reservation saved = repository.save(reservation);

    // ✅ Email dans try-catch — ne bloque plus la réservation
    try {
        emailService.envoyerConfirmation(saved);
    } catch (Exception e) {
        System.err.println("⚠️ Email non envoyé (ignoré en dev) : " + e.getMessage());
    }

    return mapper.toResponse(saved);  // ← retourne toujours 200 OK
}

    @Override
    public ReservationResponse updateStatut(String id, StatutReservation statut) {
        Reservation reservation = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée : " + id));
        reservation.setStatut(statut);
        return mapper.toResponse(repository.save(reservation));
    }

    @Override
    public void deleteReservation(String id) {
        repository.deleteById(id);
    }

    @Override
    public List<ReservationResponse> getHistoriqueUtilisateur(String utilisateurId) {
        return repository.findByUtilisateurId(utilisateurId)
                .stream().map(mapper::toResponse).collect(Collectors.toList());
    }
}