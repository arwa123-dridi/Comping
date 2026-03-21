package tn.comping.spring.backendcomping.utils.mapper;

import tn.comping.spring.backendcomping.dto.StatistiquesAvisDTO;
import tn.comping.spring.backendcomping.entities.Avis;

import java.util.List;

/**
 * Mapper pour calculer et créer les statistiques des avis
 */
public class StatistiquesAvisMapper {

    /**
     * Calculer les statistiques à partir d'une liste d'avis
     */
    public static StatistiquesAvisDTO toDTO(List<Avis> avisList) {
        if (avisList == null || avisList.isEmpty()) {
            return StatistiquesAvisDTO.builder()
                    .nombreTotal(0)
                    .noteMoyenne(0.0)
                    .nombre5Etoiles(0)
                    .nombre4Etoiles(0)
                    .nombre3Etoiles(0)
                    .nombre2Etoiles(0)
                    .nombre1Etoile(0)
                    .build();
        }

        long nombreTotal = avisList.size();

        // Calculer la note moyenne
        double noteMoyenne = avisList.stream()
                .mapToInt(Avis::getNote)
                .average()
                .orElse(0.0);

        // Arrondir à 1 décimale
        noteMoyenne = Math.round(noteMoyenne * 10.0) / 10.0;

        // Compter les avis par note
        long nombre5Etoiles = avisList.stream().filter(a ->
a.getNote() == 5).count();
        long nombre4Etoiles = avisList.stream().filter(a ->
a.getNote() == 4).count();
        long nombre3Etoiles = avisList.stream().filter(a ->
a.getNote() == 3).count();
        long nombre2Etoiles = avisList.stream().filter(a ->
a.getNote() == 2).count();
        long nombre1Etoile = avisList.stream().filter(a -> a.getNote()
== 1).count();

        return StatistiquesAvisDTO.builder()
                .nombreTotal(nombreTotal)
                .noteMoyenne(noteMoyenne)
                .nombre5Etoiles(nombre5Etoiles)
                .nombre4Etoiles(nombre4Etoiles)
                .nombre3Etoiles(nombre3Etoiles)
                .nombre2Etoiles(nombre2Etoiles)
                .nombre1Etoile(nombre1Etoile)
                .build();
    }

    /**
     * Calculer le pourcentage pour chaque note
     */
    public static double calculerPourcentage(long nombreNote, long
nombreTotal) {
        if (nombreTotal == 0) {
            return 0.0;
        }
        return Math.round((nombreNote * 100.0 / nombreTotal) * 10.0) / 10.0;
    }
}