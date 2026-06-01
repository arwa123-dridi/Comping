package tn.comping.spring.backendcomping.Testunitaire.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import tn.comping.spring.backendcomping.entities.CarteFidelite;
import tn.comping.spring.backendcomping.repositories.CarteFideliteRepository;
import tn.comping.spring.backendcomping.services.serviceImpl.CarteFideliteServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CarteFideliteServiceImplTest {

    @Mock
    private CarteFideliteRepository carteFideliteRepository;

    @InjectMocks
    private CarteFideliteServiceImpl carteFideliteService;

    private CarteFidelite carteSample;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        carteSample = new CarteFidelite();
        carteSample.setId("carte-1");
        carteSample.setClientId("client-1");
        carteSample.setPoints(0);
        carteSample.setNiveau("Bronze");
    }

    // ─────────────────────────────────────────────
    //  1. getOrCreate — carte existante
    // ─────────────────────────────────────────────
    @Test
    void testGetOrCreate_CarteExistante() {
        when(carteFideliteRepository.findByClientId("client-1"))
                .thenReturn(Optional.of(carteSample));

        CarteFidelite result = carteFideliteService.getOrCreate("client-1");

        assertNotNull(result);
        assertEquals("client-1", result.getClientId());
        assertEquals(0, result.getPoints());

        verify(carteFideliteRepository, times(1)).findByClientId("client-1");
        verify(carteFideliteRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────
    //  2. getOrCreate — carte inexistante → création
    // ─────────────────────────────────────────────
    @Test
    void testGetOrCreate_CarteInexistante_Creation() {
        when(carteFideliteRepository.findByClientId("client-new"))
                .thenReturn(Optional.empty());
        when(carteFideliteRepository.save(any(CarteFidelite.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CarteFidelite result = carteFideliteService.getOrCreate("client-new");

        assertNotNull(result);
        assertEquals("client-new", result.getClientId());
        assertEquals(0, result.getPoints());
        assertEquals("Bronze", result.getNiveau());

        verify(carteFideliteRepository, times(1)).save(any(CarteFidelite.class));
    }

    // ─────────────────────────────────────────────
    //  3. ajouterPoints
    // ─────────────────────────────────────────────
    @Test
    void testAjouterPoints() {
        carteSample.setPoints(50);

        when(carteFideliteRepository.findByClientId("client-1"))
                .thenReturn(Optional.of(carteSample));
        when(carteFideliteRepository.save(any(CarteFidelite.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        carteFideliteService.ajouterPoints("client-1", 60); // 50 + 60 = 110

        assertEquals(110, carteSample.getPoints());
        assertEquals("Bronze", carteSample.getNiveau()); // 110 < 200 → Bronze

        verify(carteFideliteRepository, times(1)).save(carteSample);
    }

    @Test
    void testAjouterPoints_PassageNiveauSilver() {
        carteSample.setPoints(150);

        when(carteFideliteRepository.findByClientId("client-1"))
                .thenReturn(Optional.of(carteSample));
        when(carteFideliteRepository.save(any(CarteFidelite.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        carteFideliteService.ajouterPoints("client-1", 60); // 150 + 60 = 210 → Silver

        assertEquals(210, carteSample.getPoints());
        assertEquals("Silver", carteSample.getNiveau());

        verify(carteFideliteRepository, times(1)).save(carteSample);
    }

    @Test
    void testAjouterPoints_PassageNiveauGold() {
        carteSample.setPoints(450);

        when(carteFideliteRepository.findByClientId("client-1"))
                .thenReturn(Optional.of(carteSample));
        when(carteFideliteRepository.save(any(CarteFidelite.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        carteFideliteService.ajouterPoints("client-1", 60); // 450 + 60 = 510 → Gold

        assertEquals(510, carteSample.getPoints());
        assertEquals("Gold", carteSample.getNiveau());

        verify(carteFideliteRepository, times(1)).save(carteSample);
    }

    // ─────────────────────────────────────────────
    //  4. updateNiveau
    // ─────────────────────────────────────────────
    @Test
    void testUpdateNiveau_Bronze() {
        carteSample.setPoints(50);
        carteFideliteService.updateNiveau(carteSample);
        assertEquals("Bronze", carteSample.getNiveau());
    }

    @Test
    void testUpdateNiveau_Silver() {
        carteSample.setPoints(200);
        carteFideliteService.updateNiveau(carteSample);
        assertEquals("Silver", carteSample.getNiveau());
    }

    @Test
    void testUpdateNiveau_Gold() {
        carteSample.setPoints(500);
        carteFideliteService.updateNiveau(carteSample);
        assertEquals("Gold", carteSample.getNiveau());
    }

    // ─────────────────────────────────────────────
    //  5. calculerReduction
    // ─────────────────────────────────────────────
    @Test
    void testCalculerReduction_Aucune() {
        carteSample.setPoints(50); // < 100 → pas de réduction

        when(carteFideliteRepository.findByClientId("client-1"))
                .thenReturn(Optional.of(carteSample));

        double result = carteFideliteService.calculerReduction("client-1", 100.0);

        assertEquals(100.0, result);
    }

    @Test
    void testCalculerReduction_10Pourcent() {
        carteSample.setPoints(100); // >= 100 → -10%

        when(carteFideliteRepository.findByClientId("client-1"))
                .thenReturn(Optional.of(carteSample));

        double result = carteFideliteService.calculerReduction("client-1", 100.0);

        assertEquals(90.0, result);
    }

    @Test
    void testCalculerReduction_20Pourcent() {
        carteSample.setPoints(200); // >= 200 → -20%

        when(carteFideliteRepository.findByClientId("client-1"))
                .thenReturn(Optional.of(carteSample));

        double result = carteFideliteService.calculerReduction("client-1", 100.0);

        assertEquals(80.0, result);
    }

    // ─────────────────────────────────────────────
    //  6. consommerPoints
    // ─────────────────────────────────────────────
    @Test
    void testConsommerPoints_Moins200() {
        carteSample.setPoints(250); // >= 200 → retire 200

        when(carteFideliteRepository.findByClientId("client-1"))
                .thenReturn(Optional.of(carteSample));
        when(carteFideliteRepository.save(any(CarteFidelite.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        carteFideliteService.consommerPoints("client-1");

        assertEquals(50, carteSample.getPoints());
        assertEquals("Bronze", carteSample.getNiveau());

        verify(carteFideliteRepository, times(1)).save(carteSample);
    }

    @Test
    void testConsommerPoints_Moins100() {
        carteSample.setPoints(120); // >= 100 mais < 200 → retire 100

        when(carteFideliteRepository.findByClientId("client-1"))
                .thenReturn(Optional.of(carteSample));
        when(carteFideliteRepository.save(any(CarteFidelite.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        carteFideliteService.consommerPoints("client-1");

        assertEquals(20, carteSample.getPoints());
        assertEquals("Bronze", carteSample.getNiveau());

        verify(carteFideliteRepository, times(1)).save(carteSample);
    }

    @Test
    void testConsommerPoints_PasAssez() {
        carteSample.setPoints(50); // < 100 → rien ne se passe

        when(carteFideliteRepository.findByClientId("client-1"))
                .thenReturn(Optional.of(carteSample));
        when(carteFideliteRepository.save(any(CarteFidelite.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        carteFideliteService.consommerPoints("client-1");

        assertEquals(50, carteSample.getPoints()); // inchangé

        verify(carteFideliteRepository, times(1)).save(carteSample);
    }

    // ─────────────────────────────────────────────
    //  7. getFideliteMessage
    // ─────────────────────────────────────────────
    @Test
    void testGetFideliteMessage_MoinsDe100Points() {
        carteSample.setPoints(40);

        when(carteFideliteRepository.findByClientId("client-1"))
                .thenReturn(Optional.of(carteSample));

        String message = carteFideliteService.getFideliteMessage("client-1");

        assertTrue(message.contains("40 points"));
        assertTrue(message.contains("60 points pour -10%"));
    }

    @Test
    void testGetFideliteMessage_Entre100Et199Points() {
        carteSample.setPoints(150);

        when(carteFideliteRepository.findByClientId("client-1"))
                .thenReturn(Optional.of(carteSample));

        String message = carteFideliteService.getFideliteMessage("client-1");

        assertTrue(message.contains("150 points"));
        assertTrue(message.contains("-10%"));
    }

    @Test
    void testGetFideliteMessage_200PointsEtPlus() {
        carteSample.setPoints(250);

        when(carteFideliteRepository.findByClientId("client-1"))
                .thenReturn(Optional.of(carteSample));

        String message = carteFideliteService.getFideliteMessage("client-1");

        assertTrue(message.contains("250 points"));
        assertTrue(message.contains("-20%"));
    }
}