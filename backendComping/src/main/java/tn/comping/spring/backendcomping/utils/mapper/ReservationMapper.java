package tn.comping.spring.backendcomping.utils.mapper;

import org.springframework.stereotype.Component;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.entities.Reservation;

@Component
public class ReservationMapper {
    public Reservation toEntity(ReservationRequest req) {
        Reservation r = new Reservation();
        r.setSiteCampingId(req.getSiteCampingId());
        r.setUtilisateurId(req.getUtilisateurId());
        r.setDateDebut(req.getDateDebut()); r.setDateFin(req.getDateFin());
        r.setModePaiement(req.getModePaiement());
        return r;
    }
    public ReservationResponse toResponse(Reservation r) {
        return new ReservationResponse(r.getId(), r.getSiteCampingId(),
                r.getUtilisateurId(), r.getDateDebut(), r.getDateFin(),
                r.getStatut(), r.getMontantTotal(), r.getModePaiement());
    }
}