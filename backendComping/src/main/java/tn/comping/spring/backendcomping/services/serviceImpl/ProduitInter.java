package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.RequestProduitDTO;
import tn.comping.spring.backendcomping.dto.ResponseProduitDTO;
import java.util.List;

public interface ProduitInter {

    ResponseProduitDTO addProduit(RequestProduitDTO produitDTO);

    List<ResponseProduitDTO> getAllProduits();

    ResponseProduitDTO updateProduit(String id, RequestProduitDTO produitDTO);

    String deleteProduit(String id);
}