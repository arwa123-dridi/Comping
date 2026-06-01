package tn.comping.spring.backendcomping.TestIntegration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import tn.comping.spring.backendcomping.entities.Produit;
import tn.comping.spring.backendcomping.entities.statutProduit;
import tn.comping.spring.backendcomping.repositories.ProduitRepository;
import tn.comping.spring.backendcomping.services.serviceImpl.EmailServiceProduct;

import java.io.File;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProduitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProduitRepository produitRepository;

    @MockBean
    private EmailServiceProduct emailService;

    @BeforeEach
    void setup() {
        produitRepository.deleteAll();

        // éviter crash email
        doNothing().when(emailService).sendOutOfStockEmail(any(Produit.class));

        // IMPORTANT: éviter erreur upload dossier
        new File("uploads/products").mkdirs();
    }

    // ================= CREATE PRODUCT =================
    @Test
    void shouldCreateProduit() throws Exception {

        String produitJson = """
        {
            "nomProduit": "Tente Camping",
            "descriptionProduit": "Test description",
            "prixProduit": 100.0,
            "quantiteStock": 10,
            "seuilAlerteStock": 2,
            "categorieProduit": "TENTES"
        }
        """;

        MockMultipartFile produit = new MockMultipartFile(
                "produit",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                produitJson.getBytes()
        );

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        // EXECUTE REQUEST
        String response = mockMvc.perform(multipart("/api/produits/addProduct")
                        .file(produit)
                        .file(image))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomProduit").value("Tente Camping"))
                .andExpect(jsonPath("$.prixProduit").value(100.0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // PRINT RESPONSE (DEBUG IMPORTANT)
        System.out.println("===== RESPONSE API =====");
        System.out.println(response);

        // VERIFY DATABASE
        List<Produit> list = produitRepository.findAll();

        System.out.println("===== DB CONTENT =====");
        list.forEach(System.out::println);

        assertThat(list.size(), is(1));
        assertThat(list.get(0).getNomProduit(), is("Tente Camping"));
    }

    // ================= GET ALL =================
    @Test
    void shouldGetAllProduits() throws Exception {

        Produit p = new Produit();
        p.setNomProduit("Tente");
        p.setPrixProduit(50.0);
        p.setQuantiteStock(5);
        p.setStatut(statutProduit.DISPONIBLE);

        produitRepository.save(p);

        mockMvc.perform(get("/api/produits/allProduct"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomProduit").value("Tente"));
    }

    // ================= GET BY ID =================
    @Test
    void shouldGetProduitById() throws Exception {

        Produit p = new Produit();
        p.setNomProduit("Sac");
        p.setPrixProduit(30.0);
        p.setQuantiteStock(3);
        p.setStatut(statutProduit.DISPONIBLE);

        Produit saved = produitRepository.save(p);

        mockMvc.perform(get("/api/produits/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomProduit").value("Sac"));
    }

    // ================= DELETE =================
    @Test
    void shouldDeleteProduit() throws Exception {

        Produit p = new Produit();
        p.setNomProduit("DeleteTest");
        p.setPrixProduit(10.0);
        p.setQuantiteStock(1);

        Produit saved = produitRepository.save(p);

        mockMvc.perform(delete("/api/produits/deleteProduct/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("Produit deleted successfully"));
    }

    // ================= SEARCH =================
    @Test
    void shouldSearchProduits() throws Exception {

        Produit p = new Produit();
        p.setNomProduit("Tente Luxe");
        p.setPrixProduit(200.0);
        p.setQuantiteStock(5);

        produitRepository.save(p);

        mockMvc.perform(get("/api/produits/search")
                        .param("nom", "tente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomProduit").value("Tente Luxe"));
    }
}