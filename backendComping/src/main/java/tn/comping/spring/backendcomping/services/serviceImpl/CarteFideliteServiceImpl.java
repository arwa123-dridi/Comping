package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.entities.CarteFidelite;
import tn.comping.spring.backendcomping.repositories.CarteFideliteRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarteFideliteServiceImpl implements CarteFideliteService{
    private  final CarteFideliteRepository carteFideliteRepository;
    @Override
    public CarteFidelite getOrCreate(String clientId) {
        return carteFideliteRepository.findByClientId(clientId)
                .orElseGet(() -> {
                    CarteFidelite c = new CarteFidelite();
                    c.setClientId(clientId);
                    return carteFideliteRepository.save(c);
                });
    }

    @Override
    public void ajouterPoints(String clientId, int montant) {
        CarteFidelite carte = getOrCreate(clientId);

        carte.setPoints(carte.getPoints() + montant);

        updateNiveau(carte);

        carteFideliteRepository.save(carte);
    }

    @Override
    public double appliquerReduction(String clientId, double prix) {
        CarteFidelite carte = getOrCreate(clientId);

        if (carte.getPoints() >= 200) {
            carte.setPoints(carte.getPoints() - 200);
            carteFideliteRepository.save(carte);
            return prix * 0.8;
        }

        if (carte.getPoints() >= 100) {
            carte.setPoints(carte.getPoints() - 100);
            carteFideliteRepository.save(carte);
            return prix * 0.9;
        }

        return prix;
    }

    @Override
    public void updateNiveau(CarteFidelite carte) {
        if (carte.getPoints() >= 500) {
            carte.setNiveau("Gold");
        } else if (carte.getPoints() >= 200) {
            carte.setNiveau("Silver");
        } else {
            carte.setNiveau("Bronze");
        }
    }

}
