package tn.comping.spring.backendcomping.services.serviceImpl;

import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.entities.CarteFidelite;

@Service
public interface CarteFideliteService {
     CarteFidelite getOrCreate(String clientId);
     void ajouterPoints(String clientId, int montant);
     double appliquerReduction(String clientId, double prix);
    void updateNiveau(CarteFidelite carte);

}
