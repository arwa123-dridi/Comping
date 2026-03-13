package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.ProduitDTO;
import tn.comping.spring.backendcomping.entities.Produit;
import tn.comping.spring.backendcomping.services.serviceImpl.ProduitInter;


import java.util.List;

@RestController
@RequestMapping("/api/produits")
@RequiredArgsConstructor
public class ProduitController {

    private final ProduitInter produitService;

    @PostMapping("/addProduct")
    public Produit addProduit(@RequestBody ProduitDTO produitDTO) {
        return produitService.addProduit(produitDTO);
    }

    @GetMapping("/allProduct")
    public List<Produit> getAllProduits() {
        return produitService.getAllProduits();
    }

    @PutMapping("/updateProduct/{id}")
    public String updateProduit(@PathVariable String id,
                                @RequestBody ProduitDTO produitDTO) {
        return produitService.updateProduit(id, produitDTO);
    }

    @DeleteMapping("/deleteProduct/{id}")
    public String deleteProduit(@PathVariable String id) {
        return produitService.deleteProduit(id);
    }

}