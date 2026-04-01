package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.RequestProduitDTO;
import tn.comping.spring.backendcomping.dto.ResponseProduitDTO;
import tn.comping.spring.backendcomping.services.serviceImpl.ProduitInter;
import tn.comping.spring.backendcomping.utils.Constants;
import java.util.List;

@RestController
@RequestMapping(Constants.BASE_URL_PRODUIT)
@RequiredArgsConstructor
public class ProduitController {

    private final ProduitInter produitService;

    @PostMapping(Constants.CREATE_PRODUIT)
    public ResponseProduitDTO addProduit(@RequestBody RequestProduitDTO produitDTO) {
        return produitService.addProduit(produitDTO);
    }

    @GetMapping(Constants.GET_ALL_PRODUITS)
    public List<ResponseProduitDTO> getAllProduits() {
        return produitService.getAllProduits();
    }

    @PutMapping(Constants.UPDATE_PRODUIT)
    public ResponseProduitDTO updateProduit(@PathVariable String id,
                                            @RequestBody RequestProduitDTO produitDTO) {
        return produitService.updateProduit(id, produitDTO);
    }

    @DeleteMapping(Constants.DELETE_PRODUIT)
    public String deleteProduit(@PathVariable String id) {
        return produitService.deleteProduit(id);
    }
}