package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.PaiementResponse;
import tn.comping.spring.backendcomping.dto.StripeSessionResponseDTO;
import tn.comping.spring.backendcomping.entities.Paiement;

import java.util.List;

public interface PaiementService {
    PaiementResponse createPaiement(String reservationId, double montant, String methode);
    PaiementResponse validerPaiement(String paiementId);
    PaiementResponse rembourserPaiement(String paiementId);
    PaiementResponse getByReservationId(String reservationId);
    List<PaiementResponse> getAll();
    
    StripeSessionResponseDTO createCheckoutSession(String commandeId);
}
