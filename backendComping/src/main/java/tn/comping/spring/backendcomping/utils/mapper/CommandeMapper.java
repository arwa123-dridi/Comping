package tn.comping.spring.backendcomping.utils.mapper;

import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.entities.*;

import java.util.List;
import java.util.stream.Collectors;

public class CommandeMapper {

        // =========================
        // REQUEST → ENTITY
        // =========================
        public static CommandeProduct toEntity(CommandeRequestDTO dto) {

                if (dto == null)
                        return null;

                CommandeProduct commande = new CommandeProduct();

                // User
                commande.setUserId(dto.getUserId());

                // ⭐ Directly use the AdresseLivraison object from the DTO
                commande.setAdresseLivraison(dto.getAdresseLivraison());

                // Default status when order is created
                commande.setStatutCommande(StatutCommande.EN_ATTENTE);

                return commande;
        }

        // =========================
        // ENTITY → RESPONSE
        // =========================
        public static CommandeResponseDTO toResponse(CommandeProduct commande) {

                if (commande == null)
                        return null;

                AdresseLivraison a = commande.getAdresseLivraison();

                return CommandeResponseDTO.builder()
                                .id(commande.getId())
                                .userId(commande.getUserId())

                                // ✔ FIX: return FULL OBJECT OR FLAT (choose ONE)
                                .adresseLivraison(a != null ? mapAdresse(a) : null)

                                .totalProduits(commande.getTotalProduits())
                                .fraisLivraison(commande.getFraisLivraison())
                                .totalCommande(commande.getTotalCommande())

                                .modePaiement(commande.getModePaiement() != null
                                                ? commande.getModePaiement().name()
                                                : null)

                                .modeLivraison(commande.getModeLivraison() != null
                                                ? commande.getModeLivraison().name()
                                                : null)

                                .statut(commande.getStatutCommande() != null
                                                ? commande.getStatutCommande().name()
                                                : null)

                                .dateCommande(commande.getDateCommande())

                                .lignes(toResponseLines(commande.getLignes()))
                                .build();
        }

        // =========================
        // ADDRESS MAPPING
        // =========================
        private static AdresseLivraison mapAdresse(AdresseLivraison a) {
                if (a == null)
                        return null;

                return AdresseLivraison.builder()
                                .prenom(a.getPrenom())
                                .nom(a.getNom())
                                .telephone(a.getTelephone())
                                .adresse(a.getAdresse())
                                .ville(a.getVille())
                                .codePostal(a.getCodePostal())
                                .build();
        }

        // =========================
        // LIGNES ENTITY → RESPONSE
        // =========================
        public static List<CommandeLigneResponseDTO> toResponseLines(List<CommandeLigne> lignes) {

                if (lignes == null)
                        return List.of();

                return lignes.stream()
                                .map(l -> CommandeLigneResponseDTO.builder()
                                                .produitId(l.getProduitId())
                                                .nomProduit(l.getNomProduit())
                                                .imageUrl(l.getImageUrl())
                                                .prixUnitaire(l.getPrixUnitaire())
                                                .quantite(l.getQuantite())
                                                .sousTotal(l.getSousTotal())
                                                .build())
                                .collect(Collectors.toList());
        }

        // =========================
        // REQUEST LINES → ENTITY LINES
        // =========================
        public static List<CommandeLigne> toEntityLines(List<CommandeLigneRequestDTO> dtos) {

                if (dtos == null)
                        return List.of();

                return dtos.stream()
                                .map(dto -> {
                                        CommandeLigne l = new CommandeLigne();
                                        l.setProduitId(dto.getProduitId());
                                        l.setQuantite(dto.getQuantite());
                                        return l;
                                })
                                .collect(Collectors.toList());
        }
}