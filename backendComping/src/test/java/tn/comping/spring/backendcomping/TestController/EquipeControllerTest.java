package tn.comping.spring.backendcomping.TestController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tn.comping.spring.backendcomping.controllers.EquipeController;
import tn.comping.spring.backendcomping.dto.EquipeRequestDTO;
import tn.comping.spring.backendcomping.dto.EquipeResponseDTO;
import tn.comping.spring.backendcomping.services.serviceImpl.IEquipeService;

@WebMvcTest(EquipeController.class)
@AutoConfigureMockMvc(addFilters = false)
class EquipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IEquipeService equipeService;

    private EquipeResponseDTO response;

    @BeforeEach
    void setUp() {
        response = new EquipeResponseDTO();
        response.setId("E1");
        response.setNom("Equipe test");
    }

    @Test
    void shouldCreateEquipe() throws Exception {
        EquipeRequestDTO request = new EquipeRequestDTO();
        request.setNom("Equipe test");
        request.setDescription("Desc");
        request.setNbMembresMax(5);
        request.setNiveau("FACILE");
        request.setOrganisateurId("ORG1");

        when(equipeService.createEquipe(any())).thenReturn(response);

        mockMvc.perform(post("/api/equipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("E1"));

        verify(equipeService).createEquipe(any());
    }

    @Test
    void shouldReturnEquipeById() throws Exception {
        when(equipeService.getEquipeById("E1")).thenReturn(response);

        mockMvc.perform(get("/api/equipes/E1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("E1"));

        verify(equipeService).getEquipeById("E1");
    }
}
