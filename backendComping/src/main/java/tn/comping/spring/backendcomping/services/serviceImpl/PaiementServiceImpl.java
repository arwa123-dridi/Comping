package tn.comping.spring.backendcomping.services.serviceImpl;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.dto.PaiementResponse;
import tn.comping.spring.backendcomping.dto.StripeSessionResponseDTO;
import tn.comping.spring.backendcomping.entities.*;
import tn.comping.spring.backendcomping.repositories.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaiementServiceImpl implements PaiementService {

    private final PaiementRepository paiementRepository;
    private final ReservationRepository reservationRepository;
    private final SignupRepository signupRepository;
    private final CommandeRepository commandeRepository;
    private final EmailService emailService;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Override
    public PaiementResponse createPaiement(String reservationId, double montant, String methode) {
        try {
            Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

            Stripe.apiKey = stripeSecretKey;
            long montantCentimes = (long)(montant * 100);

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(montantCentimes > 0 ? montantCentimes : 100)
                .setCurrency("usd")
                .putMetadata("reservationId", reservationId)
                .build();

            PaymentIntent intent = PaymentIntent.create(params);

            Paiement paiement = Paiement.builder()
                .montant(montant)
                .methode(methode)
                .statut(StatutPaiement.EN_ATTENTE)
                .reservationId(reservationId)
                .stripePaymentIntentId(intent.getId())
                .stripeClientSecret(intent.getClientSecret())
                .build();

            Paiement saved = paiementRepository.save(paiement);

            reservation.setPaiementId(saved.getId());
            reservationRepository.save(reservation);

            signupRepository.findById(reservation.getUtilisateurId()).ifPresent(user -> {
                emailService.sendPaymentLink(user.getEmail(), reservationId, montant);
                log.info("Email de paiement envoyé à {}", user.getEmail());
            });

            return toResponse(saved);

        } catch (Exception e) {
            log.error("Erreur création paiement: {}", e.getMessage());
            throw new RuntimeException("Erreur: " + e.getMessage());
        }
    }

    @Override
    public StripeSessionResponseDTO createCheckoutSession(String commandeId) {
        try {
            Stripe.apiKey = stripeSecretKey;
            CommandeProduct commande = commandeRepository.findById(commandeId)
                    .orElseThrow(() -> new RuntimeException("Commande introuvable"));

            SessionCreateParams params = SessionCreateParams.builder()
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:4200/confirm-order?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("http://localhost:4200/marketplace")
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("usd")
                                    .setUnitAmount((long) (commande.getTotalCommande() * 100))
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName("Commande #" + commande.getId())
                                            .build())
                                    .build())
                            .build())
                    .putMetadata("commandeId", commandeId)
                    .putMetadata("type", "MARKETPLACE")
                    .build();

            Session session = Session.create(params);

            return StripeSessionResponseDTO.builder()
                    .sessionId(session.getId())
                    .redirectUrl(session.getUrl())
                    .build();

        } catch (Exception e) {
            log.error("Error creating Stripe session: {}", e.getMessage());
            throw new RuntimeException("Stripe error: " + e.getMessage());
        }
    }

    @Override
    public PaiementResponse validerPaiement(String paiementId) {
        Paiement paiement = paiementRepository.findById(paiementId)
            .orElseThrow(() -> new RuntimeException("Paiement non trouvé"));

        paiement.valider();
        Paiement saved = paiementRepository.save(paiement);

        reservationRepository.findById(paiement.getReservationId()).ifPresent(r -> {
            r.setStatut(StatutReservation.CONFIRME);
            r.setStatutPaiement("PAYE");
            r.setDatePaiement(new Date());
            reservationRepository.save(r);

            signupRepository.findById(r.getUtilisateurId()).ifPresent(user ->
                emailService.sendConfirmationEmail(user.getEmail(), r.getId())
            );
        });

        return toResponse(saved);
    }

    @Override
    public PaiementResponse rembourserPaiement(String paiementId) {
        Paiement paiement = paiementRepository.findById(paiementId)
            .orElseThrow(() -> new RuntimeException("Paiement non trouvé"));

        paiement.rembourser();
        Paiement saved = paiementRepository.save(paiement);

        reservationRepository.findById(paiement.getReservationId()).ifPresent(r -> {
            r.setStatut(StatutReservation.ANNULEE);
            r.setStatutPaiement("REMBOURSE");
            reservationRepository.save(r);
        });

        return toResponse(saved);
    }

    @Override
    public PaiementResponse getByReservationId(String reservationId) {
        Paiement paiement = paiementRepository.findByReservationId(reservationId)
            .orElseThrow(() -> new RuntimeException("Paiement non trouvé pour cette réservation"));
        return toResponse(paiement);
    }

    @Override
    public List<PaiementResponse> getAll() {
        return paiementRepository.findAll()
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private PaiementResponse toResponse(Paiement p) {
        PaiementResponse res = new PaiementResponse();
        res.setId(p.getId());
        res.setMontant(p.getMontant());
        res.setDatePaiement(p.getDatePaiement());
        res.setStatut(p.getStatut() != null ? p.getStatut().name() : null);
        res.setMethode(p.getMethode());
        res.setReservationId(p.getReservationId());
        res.setStripeClientSecret(p.getStripeClientSecret());
        return res;
    }
}
