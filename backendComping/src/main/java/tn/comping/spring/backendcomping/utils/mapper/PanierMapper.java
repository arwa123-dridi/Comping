package tn.comping.spring.backendcomping.utils.mapper;

import org.springframework.stereotype.Component;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.entities.*;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PanierMapper {

    // =========================
    // ENTITY → DTO
    // =========================
    public PanierResponseDTO toDto(Panier panier) {

        if (panier == null) return null;

        return PanierResponseDTO.builder()
                .id(panier.getId())
                .userId(panier.getUserId())
                .statut(panier.getStatut() != null ? panier.getStatut().name() : null)
                .totalPrice(panier.getTotalPrice())
                .lignes(toLigneDtoList(panier.getLignes()))
                .build();
    }

    // =========================
    // LIGNES LIST
    // =========================
    private List<PanierLigneResponseDTO> toLigneDtoList(List<PanierLigne> lignes) {

        if (lignes == null) return null;

        return lignes.stream()
                .map(this::toLigneDto)
                .collect(Collectors.toList());
    }

    // =========================
    // LIGNE MAPPING
    // =========================
    private PanierLigneResponseDTO toLigneDto(PanierLigne l) {

        return PanierLigneResponseDTO.builder()
                .produitId(l.getProduitId())
                .nomProduit(l.getNomProduit())
                .prixUnitaire(l.getPrixUnitaire())
                .quantite(l.getQuantite())
                .imageUrl(l.getImageUrl())
                .sousTotal(l.getSousTotal())
                .build();
    }
}