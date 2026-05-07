package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.entities.CarteFidelite;
import tn.comping.spring.backendcomping.services.serviceImpl.CarteFideliteService;
import tn.comping.spring.backendcomping.utils.Constants;

@RestController
@RequestMapping("/carte")
@RequiredArgsConstructor
public class CarteFideliteController {
    private final CarteFideliteService carteFideliteService;

    @GetMapping("/{clientId}")
    public CarteFidelite getCarte(@PathVariable String clientId) {
        return carteFideliteService.getOrCreate(clientId);
    }
    @PostMapping("/add-points")
    public String ajouterPoints(
            @RequestParam String clientId,
            @RequestParam int points) {

        carteFideliteService.ajouterPoints(clientId, points);
        return "Points ajoutés avec succès";
    }
    @GetMapping("/message/{clientId}")
    public String getMessage(@PathVariable String clientId) {
        return carteFideliteService.getFideliteMessage(clientId);
    }
}
