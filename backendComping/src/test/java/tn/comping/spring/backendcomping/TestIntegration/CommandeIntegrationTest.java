package tn.comping.spring.backendcomping.TestIntegration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tn.comping.spring.backendcomping.entities.*;
import tn.comping.spring.backendcomping.repositories.*;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CommandeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProduitRepository produitRepository;

    @Autowired
    private PanierRepository panierRepository;

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private SignupRepository signupRepository;

    @MockBean
    private tn.comping.spring.backendcomping.services.serviceImpl.EmailServiceProduct emailService;

    private Produit produit;

    private SignupEntity livreur;

    @BeforeEach
    void setup() {

        commandeRepository.deleteAll();
        panierRepository.deleteAll();
        produitRepository.deleteAll();
        signupRepository.deleteAll();

        doNothing().when(emailService).sendOutOfStockEmail(any());

        // ================= PRODUCT =================
        produit = new Produit();
        produit.setNomProduit("Tente");
        produit.setPrixProduit(100.0);
        produit.setQuantiteStock(10);
        produit.setSeuilAlerteStock(2);
        produit.setStatut(statutProduit.DISPONIBLE);

        produit = produitRepository.save(produit);

        // ================= FIX: CREATE LIVREUR =================
        livreur = new SignupEntity();
        livreur.setId("LIV1");
        livreur.setRole(Role.LIVREUR);
        livreur.setAddress("Tunis Centre");

        signupRepository.save(livreur);
    }

    @Test
    void shouldCreateCommandeAndUpdateStockFlow() throws Exception {

        // ================= PANIER =================
        Panier panier = new Panier();
        panier.setUserId("USER1");
        panier.setStatut(PanierStatut.ACTIVE);

        PanierLigne ligne = PanierLigne.builder()
                .produitId(produit.getId())
                .nomProduit(produit.getNomProduit())
                .prixUnitaire(produit.getPrixProduit())
                .quantite(2)
                .sousTotal(200.0)
                .promoActive(false)
                .build();

        panier.setLignes(List.of(ligne));
        panierRepository.save(panier);

        // ================= COMMANDE =================
        String commandeJson = """
        {
            "userId": "USER1",
            "modePaiement": "CARTE",
            "modeLivraison": "HOME_DELIVERY",
            "adresseLivraison": {
                "ville": "Tunis"
            }
        }
        """;

        mockMvc.perform(post("/api/commandes/addCommande")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commandeJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("USER1"));

        // ================= VERIFY =================
        List<CommandeProduct> commandes = commandeRepository.findAll();
        assertThat(commandes.size(), is(1));

        CommandeProduct commande = commandes.get(0);

        // ================= SET LIVREE =================
        mockMvc.perform(put("/api/commandes/updateCommande/" + commande.getId() + "/statut")
                        .param("statut", "LIVREE"))
                .andExpect(status().isOk());

        // ================= STOCK CHECK =================
        Produit updated = produitRepository.findById(produit.getId()).orElseThrow();

        assertThat(updated.getQuantiteStock(), is(8));

        System.out.println("FINAL STOCK = " + updated.getQuantiteStock());
    }
}