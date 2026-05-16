package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.entities.PaymentEvent;
import tn.comping.spring.backendcomping.entities.PaymentEventMethod;
import tn.comping.spring.backendcomping.entities.PaymentEventStatus;
import tn.comping.spring.backendcomping.repositories.PaymentEventRepository;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class PaymentEventServiceImpl implements  PaymentEventService{
    private  final PaymentEventRepository paymentEventRepository;

    @Override
    public PaymentEvent createPayment(String userId, String eventId, double amount) {
        PaymentEvent payment = PaymentEvent.builder()
                .userId(userId)
                .eventId(eventId)
                .amount(amount)
                .status(PaymentEventStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .method(PaymentEventMethod.CARD)
                .build();

        return paymentEventRepository.save(payment);
    }

    @Override
    public PaymentEvent updateStatus(String paymentId, PaymentEventStatus status) {
        PaymentEvent payment = paymentEventRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment introuvable"));

        payment.setStatus(status);

        return paymentEventRepository.save(payment);
    }
}
