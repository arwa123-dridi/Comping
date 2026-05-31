package tn.comping.spring.backendcomping.TestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tn.comping.spring.backendcomping.controllers.SignupController;
import tn.comping.spring.backendcomping.dto.SignupDTO;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.services.serviceImpl.SignupService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SignupController.class)
class SignupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SignupService signupService;

    @Autowired
    private ObjectMapper objectMapper;

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

        System.out.println("✅ registerUser test passed");
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

        System.out.println("✅ registerUser error test passed");
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

        System.out.println("✅ getUserById test passed");
    }

    // ================= GET LIVREURS =================
    @Test
    void shouldGetLivreurs() throws Exception {

        SignupEntity user = new SignupEntity();
        user.setId("1");
        user.setEmail("livreur@mail.com");

        when(signupService.getLivreurs())
                .thenReturn(List.of(user));

        mockMvc.perform(get("/api/auth/livreurs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("livreur@mail.com"));

        System.out.println("✅ getLivreurs test passed");
    }
}