package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.entities.Activity;
import tn.comping.spring.backendcomping.entities.Event;
import tn.comping.spring.backendcomping.repositories.ActivityRepository;

import java.util.AbstractMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecomendationActivityServiceImpl implements  RecomendationActivityService{
    private final ActivityRepository activityRepository;

    @Override
    public List<Activity> suggestActivities(Event event) {
        List<Activity> allActivities = activityRepository.findAll();

        return allActivities.stream()
                .map(activity -> new AbstractMap.SimpleEntry<>(activity, score(activity, event)))
                .filter(entry -> entry.getValue() > 0) // Exclure score nul
                .sorted((a, b) -> b.getValue() - a.getValue()) // Trier par score décroissant
                .map(AbstractMap.SimpleEntry::getKey)
                .collect(Collectors.toList());
    }

    private int score(Activity activity, Event event) {
        int score = 0;

        // ✅ Critère 1 : Catégorie / Type
        if (event.getCategorie() != null && activity.getType() != null) {
            String cat = event.getCategorie().toLowerCase();
            String type = activity.getType().toLowerCase();

            if (cat.equals("camping") && type.equals("aventure"))  score += 3;
            else if (cat.equals("festival") && type.equals("aventure")) score += 3; // ✅ Ajouté
            else if (cat.equals("festival") && type.equals("culture"))  score += 2; // ✅ Ajouté
            else if (cat.equals(type)) score += 3;
        }

        // ✅ Critère 2 : Niveau de difficulté
        if (event.getNiveauDifficulte() != null
                && event.getNiveauDifficulte().equalsIgnoreCase(activity.getNiveauDifficulte())) {
            score += 2;
        }

        // ✅ Critère 3 : Tranche d'âge
        if (event.getTrancheAge() != null
                && event.getTrancheAge().equalsIgnoreCase(activity.getTrancheAge())) {
            score += 2;
        }

        // ✅ Critère 4 : Saison
        if (event.getSaison() != null
                && event.getSaison().equalsIgnoreCase(activity.getSaison())) {
            score += 1;
        }

        // ✅ Critère 5 : Durée compatible (activité <= durée event)
        try {
            int activityDuree = Integer.parseInt(activity.getDuree());
            if (activityDuree > 0
                    && event.getDureeEnHeures() > 0
                    && activityDuree <= event.getDureeEnHeures()) {
                score += 1;
            }
        } catch (NumberFormatException e) {
            // Ignorer si duree n'est pas un nombre valide
        }

        // ✅ Critère 6 : Tags communs
        if (event.getTags() != null && activity.getTags() != null) {
            long commonTags = activity.getTags().stream()
                    .filter(event.getTags()::contains)
                    .count();
            score += (int) commonTags;
        }

        return score;

    }
}
