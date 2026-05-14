package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.entities.*;
import tn.comping.spring.backendcomping.repositories.PanierRepository;
import tn.comping.spring.backendcomping.repositories.ProduitRepository;
import tn.comping.spring.backendcomping.utils.mapper.PanierMapper;

import java.util.ArrayList;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class PanierServiceImpl implements PanierService {

    private final PanierRepository panierRepository;
    private final ProduitRepository produitRepository;
    private final PanierMapper panierMapper;

    @Override
    public PanierResponseDTO addProductToPanier(PanierRequestDTO request) {

        Panier panier = panierRepository
                .findByUserIdAndStatut(request.getUserId(), PanierStatut.ACTIVE)
                .orElseGet(() -> Panier.builder()
                        .userId(request.getUserId())
                        .statut(PanierStatut.ACTIVE)
                        .lignes(new ArrayList<>())
                        .totalPrice(0.0)
                        .build());

        for (PanierLigneRequestDTO item : request.getLignes()) {

            Produit p = produitRepository.findById(item.getProduitId())
                    .orElseThrow(() -> new RuntimeException("Produit not found"));

            // 🔥 CHECK IF PRODUCT ALREADY EXISTS IN CART
            PanierLigne existingLine = panier.getLignes()
                    .stream()
                    .filter(l -> l.getProduitId().equals(p.getId()))
                    .findFirst()
                    .orElse(null);

            if (existingLine != null) {

                // UPDATE EXISTING LINE
                existingLine.setQuantite(existingLine.getQuantite() + item.getQuantite());
                existingLine.setSousTotal(existingLine.getPrixUnitaire() * existingLine.getQuantite());

            } else {

                // CREATE NEW LINE
                PanierLigne newLine = PanierLigne.builder()
                        .produitId(p.getId())
                        .nomProduit(p.getNomProduit())
                        .prixUnitaire(p.getPrixProduit())
                        .quantite(item.getQuantite())
                        .imageUrl(p.getImageUrl())
                        .sousTotal(p.getPrixProduit() * item.getQuantite())
                        .build();

                panier.getLignes().add(newLine);
            }
        }

        panier.setTotalPrice(calculateTotal(panier));

        return panierMapper.toDto(panierRepository.save(panier));
    }

    @Override
    public PanierResponseDTO getPanierByUser(String userId) {

        Panier panier = panierRepository
                .findByUserIdAndStatut(userId, PanierStatut.ACTIVE)
                .orElse(null);

        return panierMapper.toDto(panier);
    }

    @Override
    public PanierResponseDTO removeProduct(String userId, String produitId) {

        Panier panier = panierRepository
                .findByUserIdAndStatut(userId, PanierStatut.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Panier not found"));

        panier.getLignes().removeIf(l -> l.getProduitId().equals(produitId));

        panier.setTotalPrice(calculateTotal(panier));

        return panierMapper.toDto(panierRepository.save(panier));
    }

    @Override
    public PanierResponseDTO updateQuantity(String userId, String produitId, Integer quantity) {

        Panier panier = panierRepository
                .findByUserIdAndStatut(userId, PanierStatut.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Panier not found"));

        for (PanierLigne l : panier.getLignes()) {
            if (l.getProduitId().equals(produitId)) {
                l.setQuantite(quantity);
                l.setSousTotal(l.getPrixUnitaire() * quantity);
            }
        }

        panier.setTotalPrice(calculateTotal(panier));

        return panierMapper.toDto(panierRepository.save(panier));
    }

    @Override
    public long getPanierCount(String userId) {

        return panierRepository.findByUserIdAndStatut(userId, PanierStatut.ACTIVE)
                .map(p -> p.getLignes()
                        .stream()
                        .mapToLong(PanierLigne::getQuantite)
                        .sum())
                .orElse(0L);
    }

    private Double calculateTotal(Panier panier) {
        return panier.getLignes()
                .stream()
                .mapToDouble(PanierLigne::getSousTotal)
                .sum();
    }

    @Override
public PanierResponseDTO clearPanier(String userId) {

    Panier panier = panierRepository
            .findByUserIdAndStatut(userId, PanierStatut.ACTIVE)
            .orElseThrow(() -> new RuntimeException("Panier not found"));

    // 🧹 remove all products
    panier.getLignes().clear();
    panier.setTotalPrice(0.0);

    Panier saved = panierRepository.save(panier);

    return panierMapper.toDto(saved);
}

}