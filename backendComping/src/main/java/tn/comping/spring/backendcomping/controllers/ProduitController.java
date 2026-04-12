package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

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
    private final ObjectMapper objectMapper;

    // ================= CREATE PRODUCT WITH IMAGE =================
    @PostMapping(value = Constants.CREATE_PRODUIT, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseProduitDTO addProduit(
            @RequestPart("produit") String produitJson,
            @RequestPart(value = "image", required = false) MultipartFile image) throws Exception {
        RequestProduitDTO produitDTO = objectMapper.readValue(produitJson, RequestProduitDTO.class);
        return produitService.addProduit(produitDTO, image);
    }

    // ================= GET ALL PRODUCTS =================
    @GetMapping(Constants.GET_ALL_PRODUITS)
    public List<ResponseProduitDTO> getAllProduits() {
        return produitService.getAllProduits();
    }

    // ================= UPDATE PRODUCT WITH IMAGE =================
    @PutMapping(value = Constants.UPDATE_PRODUIT, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseProduitDTO updateProduit(
            @PathVariable("id") String id,
            @RequestPart("produit") String produitJson,
            @RequestPart(value = "image", required = false) MultipartFile image) throws Exception {
        RequestProduitDTO produitDTO = objectMapper.readValue(produitJson, RequestProduitDTO.class);
        return produitService.updateProduit(id, produitDTO, image);
    }

    // ================= DELETE PRODUCT =================
    @DeleteMapping(Constants.DELETE_PRODUIT)
    public String deleteProduit(@PathVariable String id) {
        return produitService.deleteProduit(id);
    }

    // ================= GET PRODUCT BY ID =================
    @GetMapping("/{id}")
    public ResponseProduitDTO getProduitById(@PathVariable String id) {
        return produitService.getProduitById(id);
    }

    @GetMapping("/search")
    public List<ResponseProduitDTO> searchProduits(@RequestParam("nom") String nom) {
        return produitService.searchProduitsByName(nom);
    }
}