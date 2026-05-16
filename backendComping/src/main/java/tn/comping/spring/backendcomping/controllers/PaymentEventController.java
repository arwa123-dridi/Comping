package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.entities.PaymentEvent;
import tn.comping.spring.backendcomping.entities.PaymentEventStatus;
import tn.comping.spring.backendcomping.repositories.EventRepository;
import tn.comping.spring.backendcomping.services.serviceImpl.CarteFideliteService;
import tn.comping.spring.backendcomping.services.serviceImpl.PaymentEventService;
import tn.comping.spring.backendcomping.services.serviceImpl.StripeEventService;
import tn.comping.spring.backendcomping.utils.Constants;

@RestController
@RequestMapping("/paymentEvent")
@RequiredArgsConstructor
public class PaymentEventController {
    private final PaymentEventService paymentEventService;
    private  final StripeEventService stripeEventService;
    private final CarteFideliteService carteFideliteService;
    private final EventRepository eventRepository;
    @PostMapping("/createpaiment/{eventId}")
    public PaymentEvent createPayment(
            @PathVariable String eventId,
            @RequestParam double amount
    ) {

        String userId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return paymentEventService.createPayment(userId, eventId, amount);
    }
    @PutMapping("/status/{paymentId}")
    public PaymentEvent updateStatus(
            @PathVariable String paymentId,
            @RequestParam PaymentEventStatus status
    ) {
        return paymentEventService.updateStatus(paymentId, status);
    }
    @PostMapping("/checkout/{eventId}")
    public String checkout(@PathVariable String eventId) throws Exception {

        String userId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return stripeEventService.createCheckoutSession(eventId, userId);
    }
}
