package tn.comping.spring.backendcomping.TestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tn.comping.spring.backendcomping.controllers.PanierController;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.services.serviceImpl.PanierService;
import tn.comping.spring.backendcomping.config.JwtFilter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // IMPORTANT for JWT/security
class PanierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PanierService panierService;

    @MockBean
    private JwtFilter jwtFilter;

    // ================= GET PANIER =================
    @Test
    void shouldGetPanier() throws Exception {

        when(panierService.getPanierByUser("U1"))
                .thenReturn(new PanierResponseDTO());

        mockMvc.perform(get("/api/panier/U1"))
                .andExpect(status().isOk());

        System.out.println("✅ getPanier test passed");
    }

    // ================= ADD PRODUCT =================
    @Test
    void shouldAddProductToPanier() throws Exception {

        PanierRequestDTO request = new PanierRequestDTO();
        PanierResponseDTO response = new PanierResponseDTO();

        when(panierService.addProductToPanier(any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/panier/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        System.out.println("✅ addProductToPanier test passed");
    }

    // ================= REMOVE PRODUCT =================
    @Test
    void shouldRemoveProduct() throws Exception {

        when(panierService.removeProduct("U1", "P1"))
                .thenReturn(new PanierResponseDTO());

        mockMvc.perform(delete("/api/panier/U1/P1"))
                .andExpect(status().isOk());

        System.out.println("✅ removeProduct test passed");
    }

    // ================= UPDATE QUANTITY =================
    @Test
    void shouldUpdateQuantity() throws Exception {

        when(panierService.updateQuantity("U1", "P1", 2))
                .thenReturn(new PanierResponseDTO());

        mockMvc.perform(put("/api/panier/update")
                        .param("userId", "U1")
                        .param("produitId", "P1")
                        .param("quantity", "2"))
                .andExpect(status().isOk());

        System.out.println("✅ updateQuantity test passed");
    }

    // ================= COUNT =================
    @Test
    void shouldGetPanierCount() throws Exception {

        when(panierService.getPanierCount("U1"))
                .thenReturn(3L);

        mockMvc.perform(get("/api/panier/count/U1"))
                .andExpect(status().isOk());

        System.out.println("✅ getPanierCount test passed");
    }

    // ================= CLEAR PANIER =================
    @Test
    void shouldClearPanier() throws Exception {

        when(panierService.clearPanier("U1"))
                .thenReturn(new PanierResponseDTO());

        mockMvc.perform(delete("/api/panier/clear/U1"))
                .andExpect(status().isOk());

        System.out.println("✅ clearPanier test passed");
    }
}