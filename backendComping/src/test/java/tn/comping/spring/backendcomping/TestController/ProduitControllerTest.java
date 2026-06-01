package tn.comping.spring.backendcomping.TestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import tn.comping.spring.backendcomping.config.JwtFilter;
import tn.comping.spring.backendcomping.controllers.ProduitController;
import tn.comping.spring.backendcomping.dto.RequestProduitDTO;
import tn.comping.spring.backendcomping.dto.ResponseProduitDTO;
import tn.comping.spring.backendcomping.services.serviceImpl.ProduitInter;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ProduitController.class,
        excludeAutoConfiguration = {
                MongoDataAutoConfiguration.class,
                MongoAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ProduitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProduitInter produitService;

    @MockBean
    private JwtFilter jwtFilter;

    @Test
    void shouldGetAllProduits() throws Exception {

        when(produitService.getAllProduits())
                .thenReturn(List.of(new ResponseProduitDTO()));

        mockMvc.perform(get("/api/produits/allProduct"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetProduitById() throws Exception {

        when(produitService.getProduitById("1"))
                .thenReturn(new ResponseProduitDTO());

        mockMvc.perform(get("/api/produits/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteProduit() throws Exception {

        when(produitService.deleteProduit("1"))
                .thenReturn("Produit deleted successfully");

        mockMvc.perform(delete("/api/produits/deleteProduct/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Produit deleted successfully"));
    }

    @Test
    void shouldSearchProduits() throws Exception {

        when(produitService.searchProduitsByName("tente"))
                .thenReturn(List.of(new ResponseProduitDTO()));

        mockMvc.perform(get("/api/produits/search")
                .param("nom", "tente"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAddProduit() throws Exception {

        RequestProduitDTO request = new RequestProduitDTO();

        when(produitService.addProduit(any(), any()))
                .thenReturn(new ResponseProduitDTO());

        MockMultipartFile produit = new MockMultipartFile(
                "produit",
                "",
                "application/json",
                objectMapper.writeValueAsString(request).getBytes());

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "fake-image".getBytes());

        mockMvc.perform(
                multipart("/api/produits/addProduct")
                        .file(produit)
                        .file(image))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateProduit() throws Exception {

        RequestProduitDTO request = new RequestProduitDTO();

        when(produitService.updateProduit(anyString(), any(), any()))
                .thenReturn(new ResponseProduitDTO());

        MockMultipartFile produit = new MockMultipartFile(
                "produit",
                "",
                "application/json",
                objectMapper.writeValueAsString(request).getBytes());

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "fake-image".getBytes());

        mockMvc.perform(
                multipart("/api/produits/updateProduct/1")
                        .file(produit)
                        .file(image)
                        .with(req -> {
                            req.setMethod("PUT");
                            return req;
                        }))
                .andExpect(status().isOk());
    }
}