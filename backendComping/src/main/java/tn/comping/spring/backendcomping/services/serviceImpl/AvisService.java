package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.*;
import java.util.List;

public interface AvisService {
    AvisResponse ajouterAvis(AvisRequest request);
    List<AvisResponse> getAvisBySite(String siteCampingId);
    List<AvisResponse> getAvisByUtilisateur(String utilisateurId);
    AvisResponse modererAvis(String id, String statut);
    void supprimerAvis(String id);
}