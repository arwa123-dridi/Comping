package tn.comping.spring.backendcomping.services;

import tn.comping.spring.backendcomping.dto.AbonnementResponseDTO;
import java.util.List;
import java.util.Map;

public interface AbonnementService {
    AbonnementResponseDTO suivre(String suiveurId, String suiviId);
    void retirer(String suiveurId, String suiviId);
    List<AbonnementResponseDTO> getMesAbonnements(String suiveurId);
    boolean estSuivi(String suiveurId, String suiviId);
    Map<String, Long> getFollowStats(String userId);
}
