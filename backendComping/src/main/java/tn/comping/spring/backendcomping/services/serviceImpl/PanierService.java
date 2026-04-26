package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.PanierRequestDTO;
import tn.comping.spring.backendcomping.dto.PanierResponseDTO;

public interface PanierService {

    PanierResponseDTO getPanierByUser(String userId);

    PanierResponseDTO addProductToPanier(PanierRequestDTO request);

    PanierResponseDTO removeProduct(String userId, String produitId);

    PanierResponseDTO updateQuantity(String userId, String produitId, Integer quantity);
}