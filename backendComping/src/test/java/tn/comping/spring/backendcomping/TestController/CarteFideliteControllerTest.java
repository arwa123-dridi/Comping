package tn.comping.spring.backendcomping.TestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tn.comping.spring.backendcomping.controllers.CarteFideliteController;
import tn.comping.spring.backendcomping.entities.CarteFidelite;
import tn.comping.spring.backendcomping.services.serviceImpl.CarteFideliteService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class CarteFideliteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarteFideliteService carteFideliteService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getCarte_shouldReturnCarte() throws Exception {

        CarteFidelite carte = new CarteFidelite();
        carte.setClientId("1");

        when(carteFideliteService.getOrCreate("1")).thenReturn(carte);

        mockMvc.perform(get("/carte/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value("1"));
    }

    @Test
    void ajouterPoints_shouldReturnMessage() throws Exception {

        mockMvc.perform(post("/carte/add-points")
                        .param("clientId", "1")
                        .param("points", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string("Points ajoutés avec succès"));
    }

    @Test
    void getMessage_shouldReturnString() throws Exception {

        when(carteFideliteService.getFideliteMessage("1"))
                .thenReturn("Client fidèle");

        mockMvc.perform(get("/carte/message/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Client fidèle"));
    }
}