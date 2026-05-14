package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.dto.CommandeRequestDTO;
import tn.comping.spring.backendcomping.dto.CommandeResponseDTO;
import tn.comping.spring.backendcomping.entities.*;
import tn.comping.spring.backendcomping.repositories.CommandeRepository;
import tn.comping.spring.backendcomping.repositories.PanierRepository;
import tn.comping.spring.backendcomping.repositories.ProduitRepository;
import tn.comping.spring.backendcomping.utils.mapper.CommandeMapper;
import tn.comping.spring.backendcomping.repositories.ProduitRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommandeServiceImpl implements CommandeService {

        private final CommandeRepository commandeRepository;
        private final PanierRepository panierRepository;
        private final ProduitRepository produitRepository;
        private final DeliveryFeeService deliveryFeeService;
        private final PricingService pricingService;
        private final ProduitServiceImpl produitService;

        // =========================================================
        // 🟢 CREATE ORDER FROM ACTIVE PANIER (CHECKOUT)
        // =========================================================
        @Override
        public CommandeResponseDTO createCommande(CommandeRequestDTO dto) {

                // 1️⃣ GET ACTIVE PANIER
                Panier panier = panierRepository
                                .findByUserIdAndStatut(dto.getUserId(), PanierStatut.ACTIVE)
                                .orElseThrow(() -> new RuntimeException("Panier actif introuvable"));

                // 2️⃣ CREATE COMMANDE ENTITY
                CommandeProduct commande = new CommandeProduct();
                commande.setUserId(dto.getUserId());

                // ADDRESS
                commande.setAdresseLivraison(dto.getAdresseLivraison());

                // 3️⃣ BUILD LIGNES AVEC PRICING ENGINE
                List<CommandeLigne> lignesCommande = panier.getLignes()
                                .stream()
                                .map(p -> {

                                        Produit produit = produitRepository.findById(p.getProduitId())
                                                        .orElseThrow(() -> new RuntimeException("Produit introuvable"));

                                        // 🔥 FINAL PRICE (PROMO + NORMAL PRICE)
                                        double prixFinal = pricingService.calculateFinalPrice(produit);

                                        return CommandeLigne.builder()
                                                        .produitId(produit.getId())
                                                        .nomProduit(produit.getNomProduit())
                                                        .imageUrl(produit.getImageUrl())
                                                        .prixUnitaire(prixFinal)
                                                        .quantite(p.getQuantite())
                                                        .sousTotal(prixFinal * p.getQuantite())
                                                        .build();
                                })
                                .collect(Collectors.toList());

                commande.setLignes(lignesCommande);

                // 4️⃣ TOTAL PRODUITS (BASED ON LIGNES ❗ IMPORTANT FIX)
                double totalProduits = lignesCommande.stream()
                                .mapToDouble(CommandeLigne::getSousTotal)
                                .sum();

                // 5️⃣ DELIVERY FEE (FROM SERVICE)
                String ville = dto.getAdresseLivraison().getVille();
                ModeLivraison modeLivraison = dto.getModeLivraison();

                double fraisLivraison = deliveryFeeService.calculateFee(ville, modeLivraison);

                // 6️⃣ SET PRICING
                commande.setTotalProduits(totalProduits);
                commande.setFraisLivraison(fraisLivraison);
                commande.setTotalCommande(totalProduits + fraisLivraison);

                // 7️⃣ PAYMENT + SHIPPING DEFAULTS
                commande.setModePaiement(dto.getModePaiement()); // 🔥 FIX: use frontend value
                commande.setModeLivraison(modeLivraison);
                commande.setStatutCommande(StatutCommande.CONFIRMEE);
                commande.setDateCommande(LocalDateTime.now());

                // 8️⃣ SAVE ORDER
                CommandeProduct savedCommande = commandeRepository.save(commande);

                // 9️⃣ UPDATE PANIER
                panier.setStatut(PanierStatut.ORDERED);
                panierRepository.save(panier);

                // 🔟 RETURN DTO
                return CommandeMapper.toResponse(savedCommande);
        }

        // =========================================================
        // 🟢 GET ALL COMMANDES (ADMIN)
        // =========================================================
        @Override
        public List<CommandeResponseDTO> getAllCommandes() {
                return commandeRepository.findAll()
                                .stream()
                                .map(CommandeMapper::toResponse)
                                .collect(Collectors.toList());
        }

        // =========================================================
        // 🟢 GET COMMANDES BY USER
        // =========================================================
        @Override
        public List<CommandeResponseDTO> getCommandesByUser(String userId) {
                return commandeRepository.findByUserId(userId)
                                .stream()
                                .map(CommandeMapper::toResponse)
                                .collect(Collectors.toList());
        }

        // =========================================================
        // 🟢 UPDATE COMMANDE STATUS (ADMIN)
        // =========================================================
        @Override
        public CommandeResponseDTO updateStatut(String commandeId, String statut) {

                CommandeProduct commande = commandeRepository.findById(commandeId)
                                .orElseThrow(() -> new RuntimeException("Commande introuvable"));

                StatutCommande nouveauStatut = StatutCommande.valueOf(statut);

                // ⭐ IF ORDER BECOMES DELIVERED → DECREMENT STOCK
                if (nouveauStatut == StatutCommande.LIVREE &&
                                commande.getStatutCommande() != StatutCommande.LIVREE) {

                        for (CommandeLigne ligne : commande.getLignes()) {

                                Produit produit = produitRepository.findById(ligne.getProduitId())
                                                .orElseThrow(() -> new RuntimeException(
                                                                "Produit introuvable: " + ligne.getProduitId()));

                                int nouveauStock = produit.getQuantiteStock() - ligne.getQuantite();

                                if (nouveauStock < 0) {
                                        throw new RuntimeException(
                                                        "Stock insuffisant pour le produit: "
                                                                        + produit.getNomProduit());
                                }

                                produit.setQuantiteStock(nouveauStock);

                                // ⭐ VERY IMPORTANT: update stock status (DISPONIBLE / FAIBLE / RUPTURE)
                                produitService.updateStatutProduit(produit);

                                // ⭐ SAVE AFTER STATUS UPDATE
                                produitRepository.save(produit);
                        }
                }

                // update status after stock update
                commande.setStatutCommande(nouveauStatut);

                CommandeProduct updated = commandeRepository.save(commande);
                return CommandeMapper.toResponse(updated);
        }

        // =========================================================
        // 🟢 DELETE COMMANDE
        // =========================================================
        @Override
        public void deleteCommande(String id) {
                commandeRepository.deleteById(id);
        }

        // =========================================================
        // 🟢 GET COMMANDE BY ID
        // =========================================================
        @Override
        public CommandeResponseDTO getCommandeById(String id) {

                CommandeProduct commande = commandeRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Commande introuvable"));

                return CommandeMapper.toResponse(commande);
        }
}