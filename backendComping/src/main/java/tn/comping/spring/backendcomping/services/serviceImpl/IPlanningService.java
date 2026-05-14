package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.SortiePlanifieeDTO;
import java.util.List;

public interface IPlanningService {

    /** Retourne jusqu'à 8 sorties planifiées sur 3 mois selon l'historique */
    List<SortiePlanifieeDTO> genererPlanning(String utilisateurId);

    /** Filtre le planning sur un mois donné (format "2026-06") */
    List<SortiePlanifieeDTO> getPlanningParMois(String utilisateurId, String mois);

    /** Inscrit l'utilisateur à la sortie choisie + met à jour le profil IA */
    void validerSortie(String utilisateurId, String sortieId);
}