package tn.comping.spring.backendcomping.TestIntegration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tn.comping.spring.backendcomping.dto.EventRequestDTO;
import tn.comping.spring.backendcomping.entities.Event;
import tn.comping.spring.backendcomping.entities.StatutEvent;
import tn.comping.spring.backendcomping.repositories.EventRepository;

import java.security.Key;
import java.util.ArrayList;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "testuser", roles = "USER")
class EventIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepository eventRepository;

    // ⭐ Même secret que JwtUtils
    private static final String JWT_SECRET = "compingSecretKeyForJWTMustBe256BitsLongAtLeast!!";

    private String generateTestToken() {
        Key key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
        return Jwts.builder()
                .setSubject("testuser@test.com")
                .claim("id", "test-user-id-123")
                .claim("role", "USER")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 600000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @BeforeEach
    void setup() {
        eventRepository.deleteAll();
    }

    // ================= CREATE =================
    @Test
    void shouldCreateEvent() throws Exception {

        EventRequestDTO dto = new EventRequestDTO();
        dto.setTitre("Randonnée");
        dto.setDescription("Event test");
        dto.setPrix(50);
        dto.setCapacite(10);
        dto.setLieu("Tunis");
        dto.setActivityIds(new ArrayList<>());

        mockMvc.perform(post("/api/events/CREATE/EVENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + generateTestToken()) // ⭐ Vrai token JWT
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titre").value("Randonnée"));
    }

    // ================= GET ALL =================
    @Test
    void shouldGetAllEvents() throws Exception {

        Event event = Event.builder()
                .titre("Test Event")
                .description("desc")
                .prix(20)
                .capacite(5)
                .statut(StatutEvent.EN_ATTENTE)
                .activityIds(new ArrayList<>())
                .build();

        eventRepository.save(event);

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titre").value("Test Event"));
    }

    // ================= GET BY ID =================
    @Test
    void shouldGetEventById() throws Exception {

        Event event = Event.builder()
                .titre("Event Detail")
                .statut(StatutEvent.EN_ATTENTE)
                .activityIds(new ArrayList<>())
                .build();

        Event saved = eventRepository.save(event);

        mockMvc.perform(get("/api/events/EVENTBYID/" + saved.getIdEvent()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre").value("Event Detail"));
    }

    // ================= DELETE =================
    @Test
    void shouldDeleteEvent() throws Exception {

        Event event = Event.builder()
                .titre("To Delete")
                .statut(StatutEvent.EN_ATTENTE)
                .activityIds(new ArrayList<>())
                .build();

        Event saved = eventRepository.save(event);

        mockMvc.perform(delete("/api/events/" + saved.getIdEvent()))
                .andExpect(status().isNoContent());
    }

    // ================= COUNT VALID =================
    @Test
    void shouldCountValidEvents() throws Exception {

        Event e1 = Event.builder()
                .titre("Event Valide 1")
                .description("Description 1")
                .prix(30)
                .capacite(20)
                .lieu("Tunis")
                .statut(StatutEvent.VALIDE)
                .activityIds(new ArrayList<>())
                .build();

        Event e2 = Event.builder()
                .titre("Event Valide 2")
                .description("Description 2")
                .prix(50)
                .capacite(15)
                .lieu("Sfax")
                .statut(StatutEvent.VALIDE)
                .activityIds(new ArrayList<>())
                .build();

        eventRepository.save(e1);
        eventRepository.save(e2);

        mockMvc.perform(get("/api/events/count/valide"))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }
}