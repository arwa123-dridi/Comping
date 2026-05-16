package tn.comping.spring.backendcomping.services.serviceImpl;

import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.entities.CarteFidelite;

@Service
public interface CarteFideliteService {
     CarteFidelite getOrCreate(String clientId);
     void ajouterPoints(String clientId, int points);
     double calculerReduction(String clientId, double prix);
    void updateNiveau(CarteFidelite carte);
    void consommerPoints(String clientId);
    String getFideliteMessage(String clientId);
}
