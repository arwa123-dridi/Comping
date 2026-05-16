package tn.comping.spring.backendcomping.entities;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "paymentsEvent")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEvent {
    @Id
    private String idPayment;

    private String userId;
    private String eventId;

    private double amount;

    private PaymentEventStatus status;

    private LocalDateTime createdAt;

    private PaymentEventMethod method;
}
