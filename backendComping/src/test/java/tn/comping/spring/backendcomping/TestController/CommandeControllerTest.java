package tn.comping.spring.backendcomping.TestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tn.comping.spring.backendcomping.config.JwtFilter;
import tn.comping.spring.backendcomping.dto.CommandeRequestDTO;
import tn.comping.spring.backendcomping.dto.CommandeResponseDTO;
import tn.comping.spring.backendcomping.services.serviceImpl.CommandeService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // disables Spring Security + JWT
class CommandeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommandeService commandeService;

    @MockBean
    private JwtFilter jwtFilter;

    // ================= CREATE =================
    @Test
    void shouldCreateCommande() throws Exception {

        CommandeRequestDTO request = new CommandeRequestDTO();
        CommandeResponseDTO response = new CommandeResponseDTO();

        when(commandeService.createCommande(any(CommandeRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/commandes/addCommande")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // ================= GET ALL =================
    @Test
    void shouldGetAllCommandes() throws Exception {

        when(commandeService.getAllCommandes())
                .thenReturn(List.of(new CommandeResponseDTO()));

        mockMvc.perform(get("/api/commandes/getCommandes"))
                .andExpect(status().isOk());
    }

    // ================= GET BY USER =================
    @Test
    void shouldGetByUser() throws Exception {

        when(commandeService.getCommandesByUser("U1"))
                .thenReturn(List.of(new CommandeResponseDTO()));

        mockMvc.perform(get("/api/commandes/user/U1"))
                .andExpect(status().isOk());
    }

    // ================= GET BY ID =================
    @Test
    void shouldGetById() throws Exception {

        when(commandeService.getCommandeById("C1"))
                .thenReturn(new CommandeResponseDTO());

        mockMvc.perform(get("/api/commandes/commandById/C1"))
                .andExpect(status().isOk());
    }

    // ================= DELETE =================
    @Test
    void shouldDeleteCommande() throws Exception {

        doNothing().when(commandeService).deleteCommande("C1");

        mockMvc.perform(delete("/api/commandes/deleteCommande/C1"))
                .andExpect(status().isOk());
    }

    // ================= UPDATE STATUS =================
    @Test
    void shouldUpdateStatut() throws Exception {

        when(commandeService.updateStatut("C1", "LIVREE"))
                .thenReturn(new CommandeResponseDTO());

        mockMvc.perform(put("/api/commandes/updateCommande/C1/statut")
                        .param("statut", "LIVREE"))
                .andExpect(status().isOk());
    }

    // ================= ASSIGN LIVREUR =================
    @Test
    void shouldAssignLivreur() throws Exception {

        when(commandeService.assignLivreurToCommande("C1", "L1"))
                .thenReturn(new CommandeResponseDTO());

        mockMvc.perform(put("/api/commandes/C1/assign/L1"))
                .andExpect(status().isOk());
    }

    // ================= MARK AS LIVREE =================
    @Test
    void shouldMarkAsLivree() throws Exception {

        when(commandeService.markAsLivree("C1", "L1"))
                .thenReturn(new CommandeResponseDTO());

        mockMvc.perform(put("/api/commandes/C1/livree/L1"))
                .andExpect(status().isOk());
    }
}