package tn.comping.spring.backendcomping.services;

import tn.comping.spring.backendcomping.entities.PaymentEvent;
import tn.comping.spring.backendcomping.entities.PaymentEventMethod;
import tn.comping.spring.backendcomping.entities.PaymentEventStatus;

public interface PaymentEventService {
    PaymentEvent createPayment(String userId, String eventId, double amount);

    PaymentEvent updateStatus(String paymentId, PaymentEventStatus status);

}
