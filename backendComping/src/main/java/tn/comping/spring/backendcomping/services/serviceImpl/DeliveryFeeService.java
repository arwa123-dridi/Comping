package tn.comping.spring.backendcomping.services.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import tn.comping.spring.backendcomping.entities.ModeLivraison;

@Service
public class DeliveryFeeService {

    private final List<String> northCities = List.of(
            "Tunis","Ariana","Ben Arous","Manouba",
            "Bizerte","Nabeul","Zaghouan","Beja","Jendouba"
    );

    private final List<String> centerCities = List.of(
            "Sousse","Monastir","Mahdia","Kairouan","Sfax"
    );

    private final List<String> southWestCities = List.of(
            "Gabes","Medenine","Tataouine","Gafsa",
            "Tozeur","Kebili","Kasserine","Sidi Bouzid"
    );

    public double calculateFee(String ville, ModeLivraison modeLivraison) {

        // 🟢 retrait magasin = gratuit
        if (modeLivraison == ModeLivraison.STORE_PICKUP) {
            return 0.0;
        }

        if (ville == null || ville.isEmpty()) {
            return 10.0; // valeur fallback sécurité
        }

        // 🟢 NORD
        if (northCities.contains(ville)) {
            return 8.0;
        }

        // 🟢 CENTRE
        if (centerCities.contains(ville)) {
            return 10.0;
        }

        // 🟢 SUD / OUEST
        if (southWestCities.contains(ville)) {
            return 15.0;
        }

        // 🟡 fallback sécurité
        return 12.0;
    }
}
