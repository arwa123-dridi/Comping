package tn.comping.spring.backendcomping.TestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tn.comping.spring.backendcomping.dto.SignupDTO;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.services.serviceImpl.SignupService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SignupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SignupService signupService;

    // ================= REGISTER USER =================
    @Test
    void shouldRegisterUser() throws Exception {

        SignupDTO dto = new SignupDTO();

        SignupEntity user = new SignupEntity();
        user.setId("1");
        user.setEmail("test@mail.com");

        when(signupService.registerUser(any())).thenReturn(user);

        mockMvc.perform(post("/api/auth/registerUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.email").value("test@mail.com"));
    }

    // ================= REGISTER USER ERROR =================
    @Test
    void shouldReturnBadRequestWhenRegisterFails() throws Exception {

        SignupDTO dto = new SignupDTO();

        when(signupService.registerUser(any()))
                .thenThrow(new RuntimeException("User already exists"));

        mockMvc.perform(post("/api/auth/registerUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User already exists"));
    }

    // ================= GET USER BY ID =================
    @Test
    void shouldGetUserById() throws Exception {

        SignupEntity user = new SignupEntity();
        user.setId("1");
        user.setEmail("test@mail.com");

        when(signupService.getUserById("1")).thenReturn(user);

        mockMvc.perform(get("/api/auth/getUserById/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }

    // ================= GET LIVREURS =================
    @Test
    void shouldGetLivreurs() throws Exception {

        SignupEntity user = new SignupEntity();
        user.setId("1");
        user.setEmail("livreur@mail.com");

        when(signupService.getLivreurs()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/auth/livreurs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("livreur@mail.com"));
    }
}