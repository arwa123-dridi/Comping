package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.entities.Reservation;
import tn.comping.spring.backendcomping.entities.StatutReservation;
import java.util.List;

public interface ReservationService {
    List<ReservationResponse> getAllReservations();
    ReservationResponse getReservationById(String id);
    ReservationResponse createReservation(ReservationRequest request);
    ReservationResponse updateStatut(String id, StatutReservation statut);
    Reservation updateReservation(String id, Reservation request);
    void deleteReservation(String id);
    List<ReservationResponse> getHistoriqueUtilisateur(String utilisateurId);
}