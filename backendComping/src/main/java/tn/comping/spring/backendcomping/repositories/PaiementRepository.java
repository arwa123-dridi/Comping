package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.comping.spring.backendcomping.entities.Paiement;

import java.util.Optional;

public interface PaiementRepository extends MongoRepository<Paiement, String> {
    Optional<Paiement> findByReservationId(String reservationId);
}