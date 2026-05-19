package tn.comping.spring.backendcomping.controllers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.entities.CommandeProduct;
import tn.comping.spring.backendcomping.entities.StatutCommande;
import tn.comping.spring.backendcomping.repositories.CommandeRepository;
import tn.comping.spring.backendcomping.repositories.EventRepository;
import tn.comping.spring.backendcomping.repositories.PaymentEventRepository;
import tn.comping.spring.backendcomping.services.serviceImpl.CarteFideliteService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class StripeWebhookController {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);
    private final EventRepository eventRepository;
    private final CommandeRepository commandeRepository;
    private final CarteFideliteService carteFideliteService;
    private final PaymentEventRepository paymentEventRepository;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            HttpServletRequest request,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) throws Exception {

        byte[] payload = request.getInputStream().readAllBytes();
        String payloadStr = new String(payload, StandardCharsets.UTF_8);

        Event stripeEvent;
        try {
            stripeEvent = Webhook.constructEvent(payloadStr, sigHeader, endpointSecret);
        } catch (Exception e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        if ("checkout.session.completed".equals(stripeEvent.getType())) {
            String rawJson = stripeEvent.getDataObjectDeserializer().getRawJson();
            JsonObject sessionJson = JsonParser.parseString(rawJson).getAsJsonObject();

            String paymentStatus = sessionJson.get("payment_status").getAsString();
            if (!"paid".equals(paymentStatus)) {
                return ResponseEntity.ok("ignored");
            }

            JsonObject metadata = sessionJson.getAsJsonObject("metadata");
            if (metadata != null && metadata.has("type")) {
                String type = metadata.get("type").getAsString();
                
                if ("MARKETPLACE".equals(type)) {
                    String commandeId = metadata.get("commandeId").getAsString();
                    handleMarketplacePayment(commandeId);
                } else {
                    // Default to EVENT type for backward compatibility
                    String eventId = metadata.get("eventId").getAsString();
                    String userId = metadata.get("userId").getAsString();
                    handleEventPayment(eventId, userId);
                }
            }
        }

        return ResponseEntity.ok("OK");
    }

    private void handleMarketplacePayment(String commandeId) {
        commandeRepository.findById(commandeId).ifPresent(commande -> {
            commande.setStatutCommande(StatutCommande.CONFIRMEE);
            commandeRepository.save(commande);
            log.info("Commande #{} payée et confirmée via Stripe", commandeId);
        });
    }

    private void handleEventPayment(String eventId, String userId) {
        eventRepository.findById(eventId).ifPresent(event -> {
            if (event.getParticipantIds() == null) {
                event.setParticipantIds(new ArrayList<>());
            }
            if (!event.getParticipantIds().contains(userId)) {
                event.getParticipantIds().add(userId);
            }
            eventRepository.save(event);
            
            paymentEventRepository.findFirstByEventIdAndUserIdAndStatus(
                    eventId, userId, tn.comping.spring.backendcomping.entities.PaymentEventStatus.PENDING)
                .ifPresent(payment -> {
                    payment.setStatus(tn.comping.spring.backendcomping.entities.PaymentEventStatus.SUCCESS);
                    paymentEventRepository.save(payment);
                });
            
            carteFideliteService.ajouterPoints(userId, 50);
            log.info("Événement #{} payé par utilisateur #{} via Stripe", eventId, userId);
        });
    }
}
