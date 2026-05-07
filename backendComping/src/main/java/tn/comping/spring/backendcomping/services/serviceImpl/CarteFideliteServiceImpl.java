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
                    c.setPoints(0);
                    c.setNiveau("Bronze");
                    return carteFideliteRepository.save(c);
                });
    }

    @Override
    public void ajouterPoints(String clientId, int points) {
        CarteFidelite carte = getOrCreate(clientId);

        carte.setPoints(carte.getPoints() + points);
        updateNiveau(carte);

        carteFideliteRepository.save(carte);
    }

    @Override
    public double calculerReduction(String clientId, double prix) {
        CarteFidelite carte = getOrCreate(clientId);

        if (carte.getPoints() >= 200) {
            return prix * 0.8;
        }

        if (carte.getPoints() >= 100) {
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

    @Override
    public void consommerPoints(String clientId) {
        CarteFidelite carte = getOrCreate(clientId);

        if (carte.getPoints() >= 200) {
            carte.setPoints(carte.getPoints() - 200);
        } else if (carte.getPoints() >= 100) {
            carte.setPoints(carte.getPoints() - 100);
        }

        updateNiveau(carte);
        carteFideliteRepository.save(carte);
    }

    @Override
    public String getFideliteMessage(String clientId) {
        CarteFidelite carte = getOrCreate(clientId);

        int points = carte.getPoints();

        if (points >= 200) {
            return "Vous avez " + points + " points 🎉 Vous bénéficiez de -20% sur votre prochaine réservation.";
        }

        if (points >= 100) {
            return "Vous avez " + points + " points 👍 Vous bénéficiez de -10% sur votre prochaine réservation.";
        }

        return "Vous avez " + points + " points. Encore " + (100 - points) + " points pour -10%.";
    }

}
