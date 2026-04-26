package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.dto.PanierResponseDTO;
import tn.comping.spring.backendcomping.dto.PanierRequestDTO;
import tn.comping.spring.backendcomping.dto.PanierLigneRequestDTO;
import tn.comping.spring.backendcomping.dto.PanierLigneResponseDTO;
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

    // 🔥 GET PANIER
    @Override
    public PanierResponseDTO getPanierByUser(String userId) {

        Panier panier = panierRepository.findByUserIdAndStatut(userId, "ACTIVE")
                .orElse(null);

        return panierMapper.toDto(panier);
    }

    // 🔥 ADD PRODUCT
    @Override
    public PanierResponseDTO addProductToPanier(PanierRequestDTO request) {

        Panier panier = panierRepository.findByUserIdAndStatut(request.getUserId(), "ACTIVE")
                .orElseGet(() -> Panier.builder()
                        .userId(request.getUserId())
                        .statut("ACTIVE")
                        .lignes(new ArrayList<>())
                        .totalPrice(0.0)
                        .build());

        for (PanierLigneRequestDTO item : request.getLignes()) {

            Produit p = produitRepository.findById(item.getProduitId())
                    .orElseThrow(() -> new RuntimeException("Produit not found"));

            PanierLigne ligne = PanierLigne.builder()
                    .produitId(p.getId())
                    .nomProduit(p.getNomProduit())
                    .prixUnitaire(p.getPrixProduit())
                    .quantite(item.getQuantite())
                    .imageUrl(p.getImageUrl())
                    .sousTotal(p.getPrixProduit() * item.getQuantite())
                    .build();

            panier.getLignes().add(ligne);
        }

        panier.setTotalPrice(calculateTotal(panier));

        return panierMapper.toDto(panierRepository.save(panier));
    }

    // 🔥 REMOVE PRODUCT
    @Override
    public PanierResponseDTO removeProduct(String userId, String produitId) {

        Panier panier = panierRepository.findByUserIdAndStatut(userId, "ACTIVE")
                .orElseThrow(() -> new RuntimeException("Panier not found"));

        panier.getLignes().removeIf(l -> l.getProduitId().equals(produitId));

        panier.setTotalPrice(calculateTotal(panier));

        return panierMapper.toDto(panierRepository.save(panier));
    }

    // 🔥 UPDATE QUANTITY
    @Override
    public PanierResponseDTO updateQuantity(String userId, String produitId, Integer quantity) {

        Panier panier = panierRepository.findByUserIdAndStatut(userId, "ACTIVE")
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

    // 💰 TOTAL CALCULATION
    private Double calculateTotal(Panier panier) {

        return panier.getLignes()
                .stream()
                .mapToDouble(PanierLigne::getSousTotal)
                .sum();
    }
}