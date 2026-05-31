package tn.comping.spring.backendcomping.Testunitaire.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.entities.*;
import tn.comping.spring.backendcomping.repositories.*;
import tn.comping.spring.backendcomping.services.serviceImpl.*;
import tn.comping.spring.backendcomping.utils.mapper.CommandeMapper;

@ExtendWith(MockitoExtension.class)
class CommandeServiceImplTest {

    @Mock private CommandeRepository commandeRepository;
    @Mock private PanierRepository panierRepository;
    @Mock private ProduitRepository produitRepository;
    @Mock private DeliveryFeeService deliveryFeeService;
    @Mock private PricingService pricingService;
    @Mock private ProduitServiceImpl produitService;
    @Mock private SignupRepository signupRepository;
    @Mock private CommandeMapper commandeMapper;

    @InjectMocks
    private CommandeServiceImpl commandeService;

    private CommandeProduct commande;
    private Produit produit;
    private Panier panier;
    private SignupEntity livreur;
    private CommandeResponseDTO responseDTO;

    // ================= SETUP =================
    @BeforeEach
    void setUp() {

        produit = new Produit();
        produit.setId("P1");
        produit.setNomProduit("Tente");
        produit.setPrixProduit(100.0);
        produit.setQuantiteStock(20);

        CommandeLigne ligne = CommandeLigne.builder()
                .produitId("P1")
                .nomProduit("Tente")
                .quantite(2)
                .prixUnitaire(100.0)
                .sousTotal(200.0)
                .build();

        panier = Panier.builder()
                .userId("USER1")
                .statut(PanierStatut.ACTIVE)
                .lignes(List.of(
                        PanierLigne.builder()
                                .produitId("P1")
                                .quantite(2)
                                .build()))
                .build();

        livreur = SignupEntity.builder()
                .id("L1")
                .firstName("Ali")
                .lastName("Livreur")
                .role(Role.LIVREUR)
                .address("Tunis")
                .build();

        commande = new CommandeProduct();
        commande.setId("C1");
        commande.setUserId("USER1");
        commande.setLivreurId("L1");
        commande.setStatutCommande(StatutCommande.EXPEDIEE);
        commande.setLignes(List.of(ligne));

        responseDTO = new CommandeResponseDTO();
    }

    // ================= GET BY ID =================
    @Test
    void shouldGetCommandeById() {

        when(commandeRepository.findById("C1"))
                .thenReturn(Optional.of(commande));

        when(commandeMapper.toResponse(commande))
                .thenReturn(responseDTO);

        CommandeResponseDTO result = commandeService.getCommandeById("C1");

        assertNotNull(result);
        verify(commandeRepository).findById("C1");

        System.out.println("✅ shouldGetCommandeById PASSED");
    }

    @Test
    void shouldThrowWhenCommandeNotFound() {

        when(commandeRepository.findById("X"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> commandeService.getCommandeById("X"));

        System.out.println("✅ shouldThrowWhenCommandeNotFound PASSED");
    }

    // ================= GET ALL =================
    @Test
    void shouldGetAllCommandes() {

        when(commandeRepository.findAll())
                .thenReturn(List.of(commande));

        when(commandeMapper.toResponse(any()))
                .thenReturn(responseDTO);

        List<CommandeResponseDTO> result = commandeService.getAllCommandes();

        assertEquals(1, result.size());

        System.out.println("✅ shouldGetAllCommandes PASSED");
    }

    // ================= GET BY USER =================
    @Test
    void shouldGetCommandesByUser() {

        when(commandeRepository.findByUserId("USER1"))
                .thenReturn(List.of(commande));

        when(commandeMapper.toResponse(any()))
                .thenReturn(responseDTO);

        List<CommandeResponseDTO> result = commandeService.getCommandesByUser("USER1");

        assertEquals(1, result.size());

        System.out.println("✅ shouldGetCommandesByUser PASSED");
    }

    // ================= DELETE =================
    @Test
    void shouldDeleteCommande() {

        doNothing().when(commandeRepository).deleteById("C1");

        commandeService.deleteCommande("C1");

        verify(commandeRepository).deleteById("C1");

        System.out.println("✅ shouldDeleteCommande PASSED");
    }

    // ================= ASSIGN LIVREUR =================
    @Test
    void shouldAssignLivreurToCommande() {

        when(commandeRepository.findById("C1"))
                .thenReturn(Optional.of(commande));

        when(commandeRepository.save(any()))
                .thenReturn(commande);

        when(commandeMapper.toResponse(any()))
                .thenReturn(responseDTO);

        CommandeResponseDTO result =
                commandeService.assignLivreurToCommande("C1", "L9");

        assertNotNull(result);
        assertEquals("L9", commande.getLivreurId());
        assertEquals(StatutCommande.EXPEDIEE, commande.getStatutCommande());

        System.out.println("✅ shouldAssignLivreurToCommande PASSED");
    }

    // ================= MARK AS LIVREE =================
    @Test
    void shouldMarkAsLivree() {

        when(commandeRepository.findById("C1"))
                .thenReturn(Optional.of(commande));

        when(commandeRepository.save(any()))
                .thenReturn(commande);

        when(commandeMapper.toResponse(any()))
                .thenReturn(responseDTO);

        CommandeResponseDTO result =
                commandeService.markAsLivree("C1", "L1");

        assertNotNull(result);
        assertEquals(StatutCommande.LIVREE, commande.getStatutCommande());

        System.out.println("✅ shouldMarkAsLivree PASSED");
    }

    @Test
    void shouldThrowWhenLivreurNotAssigned() {

        commande.setLivreurId("OTHER");

        when(commandeRepository.findById("C1"))
                .thenReturn(Optional.of(commande));

        assertThrows(RuntimeException.class,
                () -> commandeService.markAsLivree("C1", "L1"));

        System.out.println("✅ shouldThrowWhenLivreurNotAssigned PASSED");
    }

    // ================= UPDATE STATUT =================
    @Test
    void shouldUpdateStatutAndDecreaseStock() {

        when(commandeRepository.findById("C1"))
                .thenReturn(Optional.of(commande));

        when(produitRepository.findById("P1"))
                .thenReturn(Optional.of(produit));

        when(commandeRepository.save(any()))
                .thenReturn(commande);

        when(commandeMapper.toResponse(any()))
                .thenReturn(responseDTO);

        commandeService.updateStatut("C1", "LIVREE");

        assertEquals(18, produit.getQuantiteStock());

        verify(produitService).updateStatutProduit(produit);
        verify(produitRepository).save(produit);

        System.out.println("✅ shouldUpdateStatutAndDecreaseStock PASSED");
    }

    @Test
    void shouldThrowWhenStockInsufficient() {

        produit.setQuantiteStock(1);

        when(commandeRepository.findById("C1"))
                .thenReturn(Optional.of(commande));

        when(produitRepository.findById("P1"))
                .thenReturn(Optional.of(produit));

        assertThrows(RuntimeException.class,
                () -> commandeService.updateStatut("C1", "LIVREE"));

        System.out.println("✅ shouldThrowWhenStockInsufficient PASSED");
    }

    // ================= CREATE COMMANDE =================
    @Test
    void shouldCreateCommandeSuccessfully() {

        AdresseLivraison adresse = AdresseLivraison.builder()
                .ville("Tunis")
                .build();

        CommandeRequestDTO request = CommandeRequestDTO.builder()
                .userId("USER1")
                .adresseLivraison(adresse)
                .modeLivraison(ModeLivraison.HOME_DELIVERY)
                .modePaiement(ModePaiement.CASH_ON_DELIVERY)
                .build();

        when(panierRepository.findByUserIdAndStatut("USER1", PanierStatut.ACTIVE))
                .thenReturn(Optional.of(panier));

        when(produitRepository.findById("P1"))
                .thenReturn(Optional.of(produit));

        when(pricingService.calculateFinalPrice(produit))
                .thenReturn(100.0);

        when(deliveryFeeService.calculateFee("Tunis", ModeLivraison.HOME_DELIVERY))
                .thenReturn(5.0);

        when(signupRepository.findByRoleAndAddressContainingIgnoreCase(Role.LIVREUR, "Tunis"))
                .thenReturn(List.of(livreur));

        when(signupRepository.findById("L1"))
                .thenReturn(Optional.of(livreur));

        when(commandeRepository.save(any()))
                .thenReturn(commande);

        when(commandeMapper.toResponse(any()))
                .thenReturn(responseDTO);

        CommandeResponseDTO result = commandeService.createCommande(request);

        assertNotNull(result);

        verify(panierRepository).save(any());
        verify(commandeRepository).save(any());

        System.out.println("✅ shouldCreateCommandeSuccessfully PASSED");
    }

    @Test
    void shouldThrowWhenPanierNotFound() {

        CommandeRequestDTO request = CommandeRequestDTO.builder()
                .userId("USER1")
                .adresseLivraison(AdresseLivraison.builder().ville("Tunis").build())
                .build();

        when(panierRepository.findByUserIdAndStatut("USER1", PanierStatut.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> commandeService.createCommande(request));

        System.out.println("✅ shouldThrowWhenPanierNotFound PASSED");
    }
}