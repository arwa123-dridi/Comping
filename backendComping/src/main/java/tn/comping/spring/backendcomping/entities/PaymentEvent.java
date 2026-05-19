package tn.comping.spring.backendcomping.entities;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "paymentsEvent")
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {
    @Id
    private String idPayment;

    private String userId;
    private String eventId;

    private double amount;

    private PaymentEventStatus status;

    private LocalDateTime createdAt;

    private PaymentEventMethod method;

    public String getIdPayment() { return idPayment; }
    public void setIdPayment(String idPayment) { this.idPayment = idPayment; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public PaymentEventStatus getStatus() { return status; }
    public void setStatus(PaymentEventStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public PaymentEventMethod getMethod() { return method; }
    public void setMethod(PaymentEventMethod method) { this.method = method; }
}
