package tn.comping.spring.backendcomping.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.EventRequestDTO;
import tn.comping.spring.backendcomping.dto.EventResponseDTO;
import tn.comping.spring.backendcomping.services.serviceImpl.EventService;
import tn.comping.spring.backendcomping.utils.Constants;

import java.util.List;


@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping(Constants.BASE_URL_EVENT)
public class EventController {

    private final EventService eventService;

    @PostMapping(Constants.CREATE_EVENT)
    public ResponseEntity<EventResponseDTO> createEvent(@RequestBody EventRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(dto));
    }

    @GetMapping(Constants.GET_EVENT_BY_ID)
    public ResponseEntity<EventResponseDTO> getEventById(@PathVariable String id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @GetMapping(Constants.GET_ALL_EVENTS)
    public ResponseEntity<List<EventResponseDTO>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @PutMapping(Constants.UPDATE_EVENT)
    public ResponseEntity<EventResponseDTO> updateEvent(@PathVariable String id,
                                                        @RequestBody EventRequestDTO dto) {
        return ResponseEntity.ok(eventService.updateEvent(id, dto));
    }

    @DeleteMapping(Constants.DELETE_EVENT)
    public ResponseEntity<Void> deleteEvent(@PathVariable String id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
