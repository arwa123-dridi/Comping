package tn.comping.spring.backendcomping.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import tn.comping.spring.backendcomping.entities.Reservation;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends MongoRepository<Reservation, String> {
    List<Reservation> findByUtilisateurId(String utilisateurId);
    List<Reservation> findBySiteCampingId(String siteCampingId);
}
