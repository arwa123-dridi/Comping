package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.services.serviceImpl.EventService;
import tn.comping.spring.backendcomping.services.serviceImpl.PaymentEventService;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class StripeWebhookController {
    private final PaymentEventService paymentEventService;
    private final EventService eventService;
    @PostMapping
    public ResponseEntity<String> handleStripeEvent(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {

        return ResponseEntity.ok("OK");
    }
}
