package tn.comping.spring.backendcomping.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.entities.Event;
import tn.comping.spring.backendcomping.entities.PaymentEventStatus;
import tn.comping.spring.backendcomping.repositories.EventRepository;
import tn.comping.spring.backendcomping.repositories.PaymentEventRepository;
import tn.comping.spring.backendcomping.services.serviceImpl.CarteFideliteService;
import tn.comping.spring.backendcomping.services.serviceImpl.EventService;
import tn.comping.spring.backendcomping.services.serviceImpl.PaymentEventService;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class StripeWebhookController {
    private final EventRepository eventRepository;
    private final CarteFideliteService carteFideliteService;
    private final PaymentEventRepository paymentEventRepository;
    private final Gson gson = new Gson();
    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) throws Exception {

        String endpointSecret = "whsec_3kirM25PDdUffMOpuMpmmtJc4Ry2Umz0";

        com.stripe.model.Event stripeEvent = Webhook.constructEvent(
                payload,
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

            eventRepository.save(event);
            paymentEventRepository.findByEventIdAndUserId(eventId, userId)
                    .ifPresent(payment -> {
                        payment.setStatus(PaymentEventStatus.SUCCESS);
                        paymentEventRepository.save(payment);
                    });
            carteFideliteService.ajouterPoints(userId, 50);
        }

        return ResponseEntity.ok("OK");

    }
}
