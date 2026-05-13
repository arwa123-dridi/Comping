package tn.comping.spring.backendcomping.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.entities.Event;
import tn.comping.spring.backendcomping.entities.PaymentEventStatus;
import tn.comping.spring.backendcomping.repositories.EventRepository;
import tn.comping.spring.backendcomping.repositories.PaymentEventRepository;
import tn.comping.spring.backendcomping.services.serviceImpl.CarteFideliteService;
import tn.comping.spring.backendcomping.services.serviceImpl.EventService;
import tn.comping.spring.backendcomping.services.serviceImpl.PaymentEventService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class StripeWebhookController {
    private final EventRepository eventRepository;
    private final CarteFideliteService carteFideliteService;
    private final PaymentEventRepository paymentEventRepository;
    private final Gson gson = new Gson();
    @Value("${stripe.webhook.secret}")
    private String endpointSecret ;
    @PostMapping
    public ResponseEntity<String> handleWebhook(
            HttpServletRequest request,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) throws Exception {

        byte[] payload = request.getInputStream().readAllBytes();
        String payloadStr = new String(payload, StandardCharsets.UTF_8);

        com.stripe.model.Event stripeEvent = Webhook.constructEvent(
                payloadStr,
                sigHeader,
                endpointSecret
        );
        if ("checkout.session.completed".equals(stripeEvent.getType())) {

            String rawJson = stripeEvent.getDataObjectDeserializer().getRawJson();
            JsonObject sessionJson = JsonParser.parseString(rawJson).getAsJsonObject();

            String paymentStatus = sessionJson.get("payment_status").getAsString();
            if (!"paid".equals(paymentStatus)) {
                return ResponseEntity.ok("ignored");
            }

            JsonObject metadata = sessionJson.getAsJsonObject("metadata");
            String eventId = metadata.get("eventId").getAsString();
            String userId = metadata.get("userId").getAsString();

            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event introuvable"));

            if (event.getParticipantIds() == null) {
                event.setParticipantIds(new ArrayList<>());
            }

            if (!event.getParticipantIds().contains(userId)) {
                event.getParticipantIds().add(userId);
            }
            boolean alreadyProcessed = paymentEventRepository
                    .existsByEventIdAndUserIdAndStatus(eventId, userId, PaymentEventStatus.SUCCESS);

            if (alreadyProcessed) {
                return ResponseEntity.ok("already processed");
            }

            eventRepository.save(event);

            paymentEventRepository.findFirstByEventIdAndUserIdAndStatus(
                            eventId, userId, PaymentEventStatus.PENDING)  // ← filtre PENDING uniquement
                    .ifPresent(payment -> {
                        payment.setStatus(PaymentEventStatus.SUCCESS);
                        paymentEventRepository.save(payment);
                    });

            carteFideliteService.ajouterPoints(userId, 50);
        }

        return ResponseEntity.ok("OK");

    }
}
