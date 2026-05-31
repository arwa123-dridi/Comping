package tn.comping.spring.backendcomping.TestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import tn.comping.spring.backendcomping.controllers.ProduitController;
import tn.comping.spring.backendcomping.dto.RequestProduitDTO;
import tn.comping.spring.backendcomping.dto.ResponseProduitDTO;
import tn.comping.spring.backendcomping.services.serviceImpl.ProduitInter;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProduitController.class)
class ProduitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProduitInter produitService;

    @Autowired
    private ObjectMapper objectMapper;

    // ================= GET ALL =================
    @Test
    void shouldGetAllProduits() throws Exception {

        when(produitService.getAllProduits())
                .thenReturn(List.of(new ResponseProduitDTO()));

        mockMvc.perform(get("/api/produits/allProduct"))
                .andExpect(status().isOk());

        System.out.println("✅ getAllProduits test passed");
    }

    // ================= GET BY ID =================
    @Test
    void shouldGetProduitById() throws Exception {

        when(produitService.getProduitById("1"))
                .thenReturn(new ResponseProduitDTO());

        mockMvc.perform(get("/api/produits/1"))
                .andExpect(status().isOk());

        System.out.println("✅ getProduitById test passed");
    }

    // ================= DELETE =================
    @Test
    void shouldDeleteProduit() throws Exception {

        when(produitService.deleteProduit("1"))
                .thenReturn("Produit deleted successfully");

        mockMvc.perform(delete("/api/produits/deleteProduct/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Produit deleted successfully"));

        System.out.println("✅ deleteProduit test passed");
    }

    // ================= SEARCH =================
    @Test
    void shouldSearchProduits() throws Exception {

        when(produitService.searchProduitsByName("tente"))
                .thenReturn(List.of(new ResponseProduitDTO()));

        mockMvc.perform(get("/api/produits/search")
                        .param("nom", "tente"))
                .andExpect(status().isOk());

        System.out.println("✅ searchProduits test passed");
    }

    // ================= ADD PRODUCT (MULTIPART) =================
    @Test
    void shouldAddProduit() throws Exception {

        RequestProduitDTO request = new RequestProduitDTO();
        ResponseProduitDTO response = new ResponseProduitDTO();

        when(produitService.addProduit(any(), any()))
                .thenReturn(response);

        MockMultipartFile produitJson = new MockMultipartFile(
                "produit",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "fake-image".getBytes()
        );

        mockMvc.perform(multipart("/api/produits/addProduct")
                        .file(produitJson)
                        .file(image))
                .andExpect(status().isOk());

        System.out.println("✅ addProduit test passed");
    }

    // ================= UPDATE PRODUCT =================
    @Test
    void shouldUpdateProduit() throws Exception {

        RequestProduitDTO request = new RequestProduitDTO();

        when(produitService.updateProduit(anyString(), any(), any()))
                .thenReturn(new ResponseProduitDTO());

        MockMultipartFile produitJson = new MockMultipartFile(
                "produit",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "fake-image".getBytes()
        );

        mockMvc.perform(multipart("/api/produits/updateProduct/1")
                        .file(produitJson)
                        .file(image)
                        .with(request1 -> {
                            request1.setMethod("PUT");
                            return request1;
                        }))
                .andExpect(status().isOk());

        System.out.println("✅ updateProduit test passed");
    }
}