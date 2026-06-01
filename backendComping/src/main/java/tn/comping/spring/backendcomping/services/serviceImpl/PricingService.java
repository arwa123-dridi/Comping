package tn.comping.spring.backendcomping.services.serviceImpl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import tn.comping.spring.backendcomping.entities.Produit;

@Service
public class PricingService {

    public double calculateFinalPrice(Produit produit) {

        if (produit.getPrixProduit() == null) {
            return 0.0;
        }

        LocalDateTime now = LocalDateTime.now();

        // 🔥 cas promo active
        if (produit.getPromoPrice() != null
                && produit.getPromoStart() != null
                && produit.getPromoEnd() != null
                && now.isAfter(produit.getPromoStart())
                && now.isBefore(produit.getPromoEnd())) {

            return produit.getPromoPrice();
        }

        return produit.getPrixProduit();
    }
}