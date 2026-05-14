package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.CommandeRequestDTO;
import tn.comping.spring.backendcomping.dto.CommandeResponseDTO;
import tn.comping.spring.backendcomping.services.serviceImpl.*;

import java.util.List;

@RestController
@RequestMapping("/api/commandes")
@RequiredArgsConstructor
@CrossOrigin("*")
public class CommandeController {

    private final CommandeService commandeService;

    // =========================
    // CREATE ORDER (CHECKOUT)
    // =========================
    @PostMapping("/addCommande")
    public CommandeResponseDTO createCommande(@RequestBody CommandeRequestDTO dto) {
        return commandeService.createCommande(dto);
    }

    // =========================
    // GET ALL ORDERS (ADMIN)
    // =========================
    @GetMapping("/getCommandes")
    public List<CommandeResponseDTO> getAll() {
        return commandeService.getAllCommandes();
    }

    // =========================
    // GET USER ORDERS
    // =========================
    @GetMapping("/user/{userId}")
    public List<CommandeResponseDTO> getByUser(@PathVariable String userId) {
        return commandeService.getCommandesByUser(userId);
    }

    // =========================
    // UPDATE STATUS (ADMIN)
    // =========================
    @PutMapping("/updateCommande/{id}/statut")
    public CommandeResponseDTO updateStatut(
            @PathVariable String id,
            @RequestParam String statut) {

        return commandeService.updateStatut(id, statut);
    }

    // =========================
    // DELETE ORDER
    // =========================
    @DeleteMapping("/deleteCommande/{id}")
    public void delete(@PathVariable String id) {
        commandeService.deleteCommande(id);
    }

    @GetMapping("commandById/{id}")
    public ResponseEntity<CommandeResponseDTO> getCommandeById(@PathVariable String id) {
        return ResponseEntity.ok(commandeService.getCommandeById(id));
    }
}