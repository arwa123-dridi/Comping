package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.RequestProduitDTO;
import tn.comping.spring.backendcomping.dto.ResponseProduitDTO;
import tn.comping.spring.backendcomping.entities.Produit;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface ProduitInter {

    List<ResponseProduitDTO> getAllProduits();
<<<<<<< HEAD

    ResponseProduitDTO addProduit(RequestProduitDTO produitDTO, MultipartFile image);

    ResponseProduitDTO updateProduit(String id, RequestProduitDTO produitDTO, MultipartFile image);

    ResponseProduitDTO getProduitById(String id);

    String deleteProduit(String id);

    List<ResponseProduitDTO> searchProduitsByName(String nomProduit);

    // ⭐ ADD THIS METHOD (for cart promo price)
    Double calculateFinalPrice(Produit produit);
=======

    ResponseProduitDTO addProduit(RequestProduitDTO produitDTO, MultipartFile image);

    ResponseProduitDTO updateProduit(String id, RequestProduitDTO produitDTO, MultipartFile image);

     ResponseProduitDTO getProduitById(String id);


    String deleteProduit(String id);

     List<ResponseProduitDTO> searchProduitsByName(String nomProduit);
>>>>>>> origin/ahmed
}