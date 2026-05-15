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
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ReservationResponse getReservationById(String id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée : " + id));
    }

    @Override
    public ReservationResponse createReservation(ReservationRequest request) {

        // 1️⃣ validation
        if (request.getSiteCampingId() == null || request.getSiteCampingId().isBlank()) {
            throw new RuntimeException("siteCampingId est obligatoire");
        }

        // 2️⃣ récupérer site camping
        SiteCamping site = siteCampingRepository.findById(request.getSiteCampingId())
                .orElseThrow(() -> new RuntimeException("Site non trouvé : " + request.getSiteCampingId()));

        if (!site.isDisponible()) {
            throw new RuntimeException("Site non disponible");
        }

        // 3️⃣ calcul durée
        long days = java.time.temporal.ChronoUnit.DAYS.between(
                request.getDateDebut().toInstant(),
                request.getDateFin().toInstant()
        );

        if (days <= 0) days = 1;

        // 4️⃣ calcul prix
        double total = days * site.getTarifs();

        // 5️⃣ mapping
        Reservation reservation = mapper.toEntity(request);

        // 6️⃣ set backend values (IMPORTANT)
        reservation.setMontantTotal(total);
        reservation.setStatut(StatutReservation.EN_ATTENTE);
        reservation.setNombrePersonnes(
            request.getNombrePersonnes() != null ? request.getNombrePersonnes() : 1
        );



        // 7️⃣ save
        Reservation saved = repository.save(reservation);

        // 8️⃣ email (non bloquant)
        try {
            emailService.envoyerConfirmation(saved);
        } catch (Exception e) {
            System.err.println("⚠️ Email error: " + e.getMessage());
        }

        return mapper.toResponse(saved);
    }

    @Override
    public Reservation updateReservation(String id, Reservation request) {

        Reservation existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée : " + id));

        // update fields
        if (request.getSiteCampingId() != null)
            existing.setSiteCampingId(request.getSiteCampingId());

        if (request.getDateDebut() != null)
            existing.setDateDebut(request.getDateDebut());

        if (request.getDateFin() != null)
            existing.setDateFin(request.getDateFin());

        if (request.getModePaiement() != null)
            existing.setModePaiement(request.getModePaiement());

        if (request.getStatut() != null)
            existing.setStatut(request.getStatut());
        
        if (request.getNombrePersonnes() != null)
            existing.setNombrePersonnes(request.getNombrePersonnes());

        // ❌ IMPORTANT : NE PAS FAIRE CA
        // existing.setMontantTotal(request.getMontantTotal());

        // 8️⃣ recalcul prix sécurisé
        SiteCamping site = siteCampingRepository.findById(existing.getSiteCampingId())
                .orElseThrow(() -> new RuntimeException("Site non trouvé"));

        long days = java.time.temporal.ChronoUnit.DAYS.between(
                existing.getDateDebut().toInstant(),
                existing.getDateFin().toInstant()
        );

        if (days <= 0) days = 1;

        existing.setMontantTotal(days * site.getTarifs());

        return repository.save(existing);
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
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
}