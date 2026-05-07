package tn.comping.spring.backendcomping.services.serviceImpl;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.entities.Event;
import tn.comping.spring.backendcomping.entities.PaymentEvent;
import tn.comping.spring.backendcomping.entities.PaymentEventMethod;
import tn.comping.spring.backendcomping.entities.PaymentEventStatus;
import tn.comping.spring.backendcomping.repositories.EventRepository;
import tn.comping.spring.backendcomping.repositories.PaymentEventRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StripeEventServiceImpl implements  StripeEventService{
    private final EventRepository eventRepository;
    private final PaymentEventRepository paymentEventRepository;
    private final CarteFideliteService carteFideliteService;
    @Override
    public String createCheckoutSession(String eventId, String userId) throws Exception {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event introuvable"));

        // ✅ juste calcul, sans modifier points
        double prixFinal = carteFideliteService.calculerReduction(userId, event.getPrix());

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:4200/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://localhost:4200/cancel")
                .putMetadata("eventId", eventId)
                .putMetadata("userId", userId)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur")
                                                .setUnitAmount((long) (prixFinal * 100))
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(event.getTitre())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        Session session = Session.create(params);

        PaymentEvent payment = PaymentEvent.builder()
                .userId(userId)
                .eventId(eventId)
                .amount(prixFinal)
                .status(PaymentEventStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .method(PaymentEventMethod.CARD)
                .build();

        paymentEventRepository.save(payment);

        return session.getUrl();

    }
}
