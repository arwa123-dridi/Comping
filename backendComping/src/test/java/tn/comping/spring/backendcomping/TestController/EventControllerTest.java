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
import tn.comping.spring.backendcomping.controllers.EventController;
import tn.comping.spring.backendcomping.dto.EventRequestDTO;
import tn.comping.spring.backendcomping.dto.EventResponseDTO;
import tn.comping.spring.backendcomping.services.serviceImpl.EventService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE = "/api/events";

    @Test
    void createEvent_shouldReturnCreated() throws Exception {

        EventRequestDTO request = new EventRequestDTO();
        request.setTitre("Hackathon");

        EventResponseDTO response = new EventResponseDTO();
        response.setIdEvent("1");

        when(eventService.createEvent(any())).thenReturn(response);

        mockMvc.perform(post(BASE + "/CREATE/EVENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idEvent").value("1"));
    }

    @Test
    void getEventById_shouldReturnEvent() throws Exception {

        EventResponseDTO response = new EventResponseDTO();
        response.setIdEvent("1");

        when(eventService.getEventById("1")).thenReturn(response);

        mockMvc.perform(get(BASE + "/EVENTBYID/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEvent").value("1"));
    }

    @Test
    void getAllEvents_shouldReturnList() throws Exception {

        EventResponseDTO response = new EventResponseDTO();
        response.setIdEvent("1");

        when(eventService.getAllEvents()).thenReturn(List.of(response));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk());
    }

    @Test
    void updateEvent_shouldReturnUpdated() throws Exception {

        EventRequestDTO request = new EventRequestDTO();
        request.setTitre("Update");

        EventResponseDTO response = new EventResponseDTO();
        response.setIdEvent("1");

        when(eventService.updateEvent(any(), any())).thenReturn(response);

        mockMvc.perform(put(BASE + "/UPDATE/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteEvent_shouldReturnNoContent() throws Exception {

        mockMvc.perform(delete(BASE + "/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void countEvents_shouldReturnOk() throws Exception {

        when(eventService.countEvents()).thenReturn(5L);

        mockMvc.perform(get(BASE + "/count"))
                .andExpect(status().isOk());
    }

    @Test
    void validerEvent_shouldReturnOk() throws Exception {

        EventResponseDTO response = new EventResponseDTO();
        response.setIdEvent("1");

        when(eventService.validerEvent("1")).thenReturn(response);

        mockMvc.perform(patch(BASE + "/1/valider"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEvent").value("1"));
    }
}