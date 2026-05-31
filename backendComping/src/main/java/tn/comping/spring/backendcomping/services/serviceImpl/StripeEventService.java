package tn.comping.spring.backendcomping.services.serviceImpl;

public interface StripeEventService {
    String createCheckoutSession(String eventId, String userId) throws Exception;
}