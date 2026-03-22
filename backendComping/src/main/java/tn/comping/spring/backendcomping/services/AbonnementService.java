package tn.comping.spring.backendcomping.services;

import tn.comping.spring.backendcomping.dto.AbonnementResponseDTO;
import java.util.List;

public interface AbonnementService {
    
    /**
     * PHASE 4 - Suivre utilisateur
     */
    AbonnementResponseDTO suivre(String suiviId, String currentUserEmail);
    
    /**
     * PHASE 4 - Ne plus suivre
     */
    void nePlusSuivre(String suiviId, String currentUserEmail);
    
    /**
     * PHASE 4 - Mes abonnements (qui je suis)
     */
    List<AbonnementResponseDTO> getMesAbonnements(String currentUserEmail);
    
    /**
     * PHASE 4 - Mes abonnés
     */
    List<AbonnementResponseDTO> getMesAbonnes(String currentUserEmail);
    
    /**
     * PHASE 4 - Stats abonnements
     */
    Object getStats(String userId);
}

