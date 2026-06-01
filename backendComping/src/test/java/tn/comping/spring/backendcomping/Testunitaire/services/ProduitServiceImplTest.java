package tn.comping.spring.backendcomping.Testunitaire.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tn.comping.spring.backendcomping.dto.ResponseProduitDTO;
import tn.comping.spring.backendcomping.entities.Produit;
import tn.comping.spring.backendcomping.entities.statutProduit;
import tn.comping.spring.backendcomping.repositories.ProduitRepository;
import tn.comping.spring.backendcomping.services.serviceImpl.EmailServiceProduct;
import tn.comping.spring.backendcomping.services.serviceImpl.ProduitServiceImpl;

@ExtendWith(MockitoExtension.class)
class ProduitServiceImplTest {

    @Mock
    private ProduitRepository produitRepository;

    @Mock
    private EmailServiceProduct emailService;

    @InjectMocks
    private ProduitServiceImpl produitService;

    private Produit produit;

    @BeforeEach
    void setUp() {

        produit = new Produit();
        produit.setId("1");
        produit.setNomProduit("Tente");
        produit.setPrixProduit(200.0);
        produit.setQuantiteStock(10);
        produit.setSeuilAlerteStock(5);
        produit.setStatut(statutProduit.DISPONIBLE);
    }

    @Test
    void shouldGetProduitById() {

        when(produitRepository.findById("1"))
                .thenReturn(Optional.of(produit));

        ResponseProduitDTO result =
                produitService.getProduitById("1");

        assertNotNull(result);

        verify(produitRepository, times(1))
                .findById("1");
    }

    @Test
    void shouldThrowExceptionWhenProduitNotFound() {

        when(produitRepository.findById("99"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> produitService.getProduitById("99"));
    }

    @Test
    void shouldDeleteProduit() {

        doNothing().when(produitRepository)
                .deleteById("1");

        String result =
                produitService.deleteProduit("1");

        assertEquals(
                "Produit deleted successfully",
                result);

        verify(produitRepository)
                .deleteById("1");
    }

    @Test
    void shouldSearchProduitsByName() {

        when(produitRepository
                .findByNomProduitContainingIgnoreCase("Tente"))
                .thenReturn(List.of(produit));

        List<ResponseProduitDTO> result =
                produitService.searchProduitsByName("Tente");

        assertEquals(1, result.size());

        verify(produitRepository, times(1))
                .findByNomProduitContainingIgnoreCase("Tente");
    }

    @Test
    void shouldSetRuptureStock() {

        produit.setStatut(statutProduit.DISPONIBLE);
        produit.setQuantiteStock(0);

        produitService.updateStatutProduit(produit);

        assertEquals(
                statutProduit.RUPTURE_STOCK,
                produit.getStatut());

        verify(emailService, times(1))
                .sendOutOfStockEmail(produit);
    }

    @Test
    void shouldSetStockFaible() {

        produit.setQuantiteStock(3);
        produit.setSeuilAlerteStock(5);

        produitService.updateStatutProduit(produit);

        assertEquals(
                statutProduit.STOCK_FAIBLE,
                produit.getStatut());
    }

    @Test
    void shouldSetDisponible() {

        produit.setQuantiteStock(50);

        produitService.updateStatutProduit(produit);

        assertEquals(
                statutProduit.DISPONIBLE,
                produit.getStatut());
    }

    @Test
    void shouldReturnPromoPriceWhenPromoActive() {

        produit.setPrixProduit(200.0);
        produit.setPromoPrice(150.0);

        produit.setPromoStart(
                LocalDateTime.now().minusDays(1));

        produit.setPromoEnd(
                LocalDateTime.now().plusDays(1));

        Double result =
                produitService.calculateFinalPrice(produit);

        assertEquals(150.0, result);
    }

    @Test
    void shouldReturnNormalPriceWhenPromoExpired() {

        produit.setPrixProduit(200.0);
        produit.setPromoPrice(150.0);

        produit.setPromoStart(
                LocalDateTime.now().minusDays(10));

        produit.setPromoEnd(
                LocalDateTime.now().minusDays(5));

        Double result =
                produitService.calculateFinalPrice(produit);

        assertEquals(200.0, result);
    }

    @Test
    void shouldReturnNormalPriceWhenNoPromoExists() {

        produit.setPrixProduit(200.0);
        produit.setPromoPrice(null);

        Double result =
                produitService.calculateFinalPrice(produit);

        assertEquals(200.0, result);
    }
}