package tn.comping.spring.backendcomping.Testunitaire.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
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
import tn.comping.spring.backendcomping.repositories.PanierRepository;
import tn.comping.spring.backendcomping.repositories.ProduitRepository;
import tn.comping.spring.backendcomping.services.serviceImpl.PanierServiceImpl;
import tn.comping.spring.backendcomping.services.serviceImpl.ProduitInter;
import tn.comping.spring.backendcomping.utils.mapper.PanierMapper;

@ExtendWith(MockitoExtension.class)
class PanierServiceImplTest {

    @Mock
    private PanierRepository panierRepository;

    @Mock
    private ProduitRepository produitRepository;

    @Mock
    private PanierMapper panierMapper;

    @Mock
    private ProduitInter produitService;

    @InjectMocks
    private PanierServiceImpl panierService;

    private Produit produit;
    private Panier panier;
    private PanierResponseDTO panierResponseDTO;

    @BeforeEach
    void setUp() {

        produit = new Produit();
        produit.setId("P1");
        produit.setNomProduit("Tente");
        produit.setPrixProduit(100.0);
        produit.setImageUrl("image.jpg");

        panier = Panier.builder()
                .userId("USER1")
                .statut(PanierStatut.ACTIVE)
                .lignes(new ArrayList<>())
                .totalPrice(0.0)
                .build();

        panierResponseDTO = new PanierResponseDTO();
    }

    @Test
    void shouldAddProductToPanier() {

        PanierLigneRequestDTO ligneRequest = new PanierLigneRequestDTO();
        ligneRequest.setProduitId("P1");
        ligneRequest.setQuantite(2);

        PanierRequestDTO request = new PanierRequestDTO();
        request.setUserId("USER1");
        request.setLignes(List.of(ligneRequest));

        when(
                panierRepository.findByUserIdAndStatut(
                        "USER1",
                        PanierStatut.ACTIVE))
                .thenReturn(Optional.empty());

        when(produitRepository.findById("P1"))
                .thenReturn(Optional.of(produit));

        when(produitService.calculateFinalPrice(produit))
                .thenReturn(100.0);

        when(panierRepository.save(any(Panier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(panierMapper.toDto(any(Panier.class)))
                .thenReturn(panierResponseDTO);

        PanierResponseDTO result =
                panierService.addProductToPanier(request);

        assertNotNull(result);

        verify(produitRepository)
                .findById("P1");

        verify(panierRepository)
                .save(any(Panier.class));
    }

    @Test
    void shouldGetPanierByUser() {

        when(
                panierRepository.findByUserIdAndStatut(
                        "USER1",
                        PanierStatut.ACTIVE))
                .thenReturn(Optional.of(panier));

        when(panierMapper.toDto(panier))
                .thenReturn(panierResponseDTO);

        PanierResponseDTO result =
                panierService.getPanierByUser("USER1");

        assertNotNull(result);
    }

    @Test
    void shouldRemoveProduct() {

        PanierLigne ligne = PanierLigne.builder()
                .produitId("P1")
                .quantite(2)
                .prixUnitaire(100.0)
                .sousTotal(200.0)
                .build();

        panier.getLignes().add(ligne);

        when(
                panierRepository.findByUserIdAndStatut(
                        "USER1",
                        PanierStatut.ACTIVE))
                .thenReturn(Optional.of(panier));

        when(panierRepository.save(any(Panier.class)))
                .thenReturn(panier);

        when(panierMapper.toDto(any(Panier.class)))
                .thenReturn(panierResponseDTO);

        PanierResponseDTO result =
                panierService.removeProduct(
                        "USER1",
                        "P1");

        assertNotNull(result);

        verify(panierRepository)
                .save(any(Panier.class));
    }

    @Test
    void shouldUpdateQuantity() {

        PanierLigne ligne = PanierLigne.builder()
                .produitId("P1")
                .quantite(1)
                .prixUnitaire(100.0)
                .sousTotal(100.0)
                .build();

        panier.getLignes().add(ligne);

        when(
                panierRepository.findByUserIdAndStatut(
                        "USER1",
                        PanierStatut.ACTIVE))
                .thenReturn(Optional.of(panier));

        when(produitRepository.findById("P1"))
                .thenReturn(Optional.of(produit));

        when(produitService.calculateFinalPrice(produit))
                .thenReturn(100.0);

        when(panierRepository.save(any(Panier.class)))
                .thenReturn(panier);

        when(panierMapper.toDto(any(Panier.class)))
                .thenReturn(panierResponseDTO);

        PanierResponseDTO result =
                panierService.updateQuantity(
                        "USER1",
                        "P1",
                        5);

        assertNotNull(result);

        assertEquals(
                5,
                panier.getLignes().get(0).getQuantite());
    }

    @Test
    void shouldGetPanierCount() {

        PanierLigne ligne1 = PanierLigne.builder()
                .quantite(2)
                .build();

        PanierLigne ligne2 = PanierLigne.builder()
                .quantite(3)
                .build();

        panier.getLignes().add(ligne1);
        panier.getLignes().add(ligne2);

        when(
                panierRepository.findByUserIdAndStatut(
                        "USER1",
                        PanierStatut.ACTIVE))
                .thenReturn(Optional.of(panier));

        long count =
                panierService.getPanierCount("USER1");

        assertEquals(5, count);
    }

    @Test
    void shouldClearPanier() {

        panier.getLignes().add(
                PanierLigne.builder()
                        .quantite(2)
                        .build());

        panier.setTotalPrice(200.0);

        when(
                panierRepository.findByUserIdAndStatut(
                        "USER1",
                        PanierStatut.ACTIVE))
                .thenReturn(Optional.of(panier));

        when(panierRepository.save(any(Panier.class)))
                .thenReturn(panier);

        when(panierMapper.toDto(any(Panier.class)))
                .thenReturn(panierResponseDTO);

        PanierResponseDTO result =
                panierService.clearPanier("USER1");

        assertNotNull(result);

        assertEquals(0, panier.getLignes().size());

        assertEquals(
                0.0,
                panier.getTotalPrice());
    }

    @Test
    void shouldThrowExceptionWhenProduitNotFound() {

        PanierLigneRequestDTO ligneRequest =
                new PanierLigneRequestDTO();

        ligneRequest.setProduitId("UNKNOWN");
        ligneRequest.setQuantite(1);

        PanierRequestDTO request =
                new PanierRequestDTO();

        request.setUserId("USER1");
        request.setLignes(List.of(ligneRequest));

        when(
                panierRepository.findByUserIdAndStatut(
                        "USER1",
                        PanierStatut.ACTIVE))
                .thenReturn(Optional.of(panier));

        when(produitRepository.findById("UNKNOWN"))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> panierService.addProductToPanier(request));
    }

    @Test
    void shouldThrowExceptionWhenPanierNotFound() {

        when(
                panierRepository.findByUserIdAndStatut(
                        "USER1",
                        PanierStatut.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> panierService.removeProduct(
                        "USER1",
                        "P1"));
    }
}