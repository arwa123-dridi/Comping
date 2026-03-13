package  tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.entities.Reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationService {

    List<Reservation> getAllReservations();

    Optional<Reservation> getReservationById(String  id);

    Reservation saveReservation(Reservation reservation);

    void deleteReservation(String  id);
}