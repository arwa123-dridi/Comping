package tn.comping.spring.backendcomping.services;

import tn.comping.spring.backendcomping.dto.FeedResponseDTO;
import java.util.List;

public interface FeedService {
    
    /**
     * PHASE 4 - Fil d'actualité (posts abonnements + publics)
     */
    List<FeedResponseDTO> getFeed(int page, int size);
}

