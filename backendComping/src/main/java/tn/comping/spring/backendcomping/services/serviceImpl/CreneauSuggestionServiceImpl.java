package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.comping.spring.backendcomping.dto.CreneauSuggestionResponse;
import tn.comping.spring.backendcomping.entities.CreneauLivraison;
import tn.comping.spring.backendcomping.repositories.CreneauLivraisonRepository;
import tn.comping.spring.backendcomping.repositories.DemandeTransportRepository;

import java.util.Comparator;
import java.util.List;

/**
 * Heuristique legere : parmi les creneaux disponibles, suggere celui qui a
 * actuellement la charge la plus faible (le moins de demandes deja assignees),
 * avec l'heure la plus matinale comme critere de depart en cas d'egalite.
 */
@Service
@RequiredArgsConstructor
public class CreneauSuggestionServiceImpl implements CreneauSuggestionService {

    private final CreneauLivraisonRepository creneauLivraisonRepository;
    private final DemandeTransportRepository demandeTransportRepository;

    @Override
    public CreneauSuggestionResponse suggest(String demandeTransportId) {
        if (!demandeTransportRepository.existsById(demandeTransportId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "DemandeTransport n'existe pas, id: " + demandeTransportId);
        }

        List<CreneauLivraison> disponibles = creneauLivraisonRepository.findAll().stream()
                .filter(CreneauLivraison::isDisponible)
                .toList();

        if (disponibles.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucun creneau de livraison disponible");
        }

        CreneauLivraison meilleur = disponibles.stream()
                .min(Comparator
                        .comparingInt((CreneauLivraison c) -> demandeTransportRepository.findByCreneauLivraisonId(c.getIdCreneauLivraison()).size())
                        .thenComparing(CreneauLivraison::getHeureDebut))
                .orElseThrow();

        int charge = demandeTransportRepository.findByCreneauLivraisonId(meilleur.getIdCreneauLivraison()).size();

        return CreneauSuggestionResponse.builder()
                .creneauLivraisonId(meilleur.getIdCreneauLivraison())
                .heureDebut(meilleur.getHeureDebut())
                .heureFin(meilleur.getHeureFin())
                .raison("Creneau disponible avec la charge la plus faible actuellement (" + charge + " demande(s) deja assignee(s))")
                .build();
    }
}
