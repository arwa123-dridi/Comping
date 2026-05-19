package tn.comping.spring.backendcomping.services;

import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.entities.StatutReservation;
import java.util.List;

public interface ReservationService {
    List<ReservationResponse> getAllReservations();
    ReservationResponse getReservationById(String id);
    ReservationResponse createReservation(ReservationRequest request);
    ReservationResponse updateStatut(String id, StatutReservation statut);
    void deleteReservation(String id);
    List<ReservationResponse> getHistoriqueUtilisateur(String utilisateurId);
}