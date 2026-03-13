package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.entities.Reservation;
import tn.comping.spring.backendcomping.repositories.ReservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository repository;

    public ReservationServiceImpl(ReservationRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Reservation> getAllReservations() {
        return repository.findAll();
    }

    @Override
    public Optional<Reservation> getReservationById(String  id) {
        return repository.findById(id);
    }

    @Override
    public Reservation saveReservation(Reservation reservation) {
        return repository.save(reservation);
    }

    @Override
    public void deleteReservation(String  id) {
        repository.deleteById(id);
    }
}