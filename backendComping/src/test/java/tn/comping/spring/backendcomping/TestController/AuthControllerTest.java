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
import tn.comping.spring.backendcomping.config.JwtUtils;
import tn.comping.spring.backendcomping.controllers.AuthController;
import tn.comping.spring.backendcomping.dto.LoginDTORequest;
import tn.comping.spring.backendcomping.dto.LoginDTOResponse;
import tn.comping.spring.backendcomping.services.serviceImpl.SignupService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SignupService signupService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private JwtFilter jwtFilter;

    @Autowired
    private ObjectMapper objectMapper;

    // ================= LOGIN =================

    @Test
    void login_shouldReturnOk() throws Exception {

        LoginDTORequest request = new LoginDTORequest();
        request.setEmail("test@mail.com");
        request.setPassword("1234");

        LoginDTOResponse response = new LoginDTOResponse("jwt-token");

        when(signupService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void login_shouldReturnBadRequest_whenException() throws Exception {

        LoginDTORequest request = new LoginDTORequest();
        request.setEmail("test@mail.com");
        request.setPassword("wrong");

        when(signupService.login(any()))
                .thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    // ================= LOGOUT =================

    @Test
    void logout_shouldReturnSuccess() throws Exception {

        String token = "valid-token";

        when(jwtUtils.validateJwtToken(token)).thenReturn(true);

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Déconnecté avec succès"));

        verify(jwtUtils).blacklistToken(token);
    }

    @Test
    void logout_shouldReturn401_whenTokenInvalid() throws Exception {

        String token = "bad-token";

        when(jwtUtils.validateJwtToken(token)).thenReturn(false);

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Token invalide ou déjà expiré"));
    }

    @Test
    void logout_shouldReturnBadRequest_whenNoToken() throws Exception {

        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Token manquant"));
    }
}