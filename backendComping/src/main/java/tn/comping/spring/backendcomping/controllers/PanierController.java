package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.PanierResponseDTO;
import tn.comping.spring.backendcomping.dto.PanierRequestDTO;

import tn.comping.spring.backendcomping.services.serviceImpl.*;;

@RestController
@RequestMapping("/api/panier")
@RequiredArgsConstructor
public class PanierController {

    private final PanierService panierService;

    @GetMapping("/{userId}")
    public PanierResponseDTO getPanier(@PathVariable String userId) {
        return panierService.getPanierByUser(userId);
    }

    @PostMapping("/add")
    public PanierResponseDTO add(@RequestBody PanierRequestDTO request) {
        return panierService.addProductToPanier(request);
    }

    @DeleteMapping("/{userId}/{produitId}")
    public PanierResponseDTO remove(
            @PathVariable String userId,
            @PathVariable String produitId) {
        return panierService.removeProduct(userId, produitId);
    }

    @PutMapping("/update")
    public PanierResponseDTO update(
            @RequestParam String userId,
            @RequestParam String produitId,
            @RequestParam Integer quantity) {
        return panierService.updateQuantity(userId, produitId, quantity);
    }
}