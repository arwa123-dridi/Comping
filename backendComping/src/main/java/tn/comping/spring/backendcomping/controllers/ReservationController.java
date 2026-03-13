package tn.comping.spring.backendcomping.controllers;

import tn.comping.spring.backendcomping.entities.Reservation;
import tn.comping.spring.backendcomping.services.serviceImpl.ReservationService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin("*")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @GetMapping
    public List<Reservation> getAll() {
        return service.getAllReservations();
    }

    @GetMapping("/{id}")
    public Optional<Reservation> getById(@PathVariable String id) {
        return service.getReservationById(id);
    }

    @PostMapping
    public Reservation create(@RequestBody Reservation reservation) {
        return service.saveReservation(reservation);
    }

    @PutMapping("/{id}")
    public Reservation update(@PathVariable String  id, @RequestBody Reservation reservation) {
        reservation.setId(id);
        return service.saveReservation(reservation);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String  id) {
        service.deleteReservation(id);
    }
}