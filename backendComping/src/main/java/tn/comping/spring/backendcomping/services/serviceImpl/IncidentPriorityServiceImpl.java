package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.entities.Incident;
import tn.comping.spring.backendcomping.entities.PrioriteIncident;
import tn.comping.spring.backendcomping.repositories.IncidentRepository;

import java.util.List;
import java.util.Locale;

/**
 * Systeme expert a base de regles : score pondere sur le type, des mots-cles
 * critiques presents dans la description, et la recurrence d'incidents de
 * l'utilisateur. Technique d'IA legere, explicable (voir buildRaison), sans
 * dependance a un modele externe.
 */
@Service
@RequiredArgsConstructor
public class IncidentPriorityServiceImpl implements IncidentPriorityService {

    private static final List<String> MOTS_CLES_CRITIQUES = List.of(
            "urgent", "accident", "blesse", "blessé", "danger", "bloque", "bloqué",
            "casse", "cassé", "fuite", "feu", "incendie", "grave"
    );

    private final IncidentRepository incidentRepository;

    @Override
    public PrioriteIncident classify(Incident incident) {
        int score = 0;

        String type = incident.getType() != null ? incident.getType().toLowerCase(Locale.ROOT) : "";
        if (type.contains("secur") || type.contains("accident")) {
            score += 40;
        } else if (type.contains("panne") || type.contains("technique")) {
            score += 25;
        } else if (type.contains("retard")) {
            score += 15;
        } else {
            score += 10;
        }

        String description = incident.getDescription() != null ? incident.getDescription().toLowerCase(Locale.ROOT) : "";
        for (String motCle : MOTS_CLES_CRITIQUES) {
            if (description.contains(motCle)) {
                score += 15;
            }
        }

        long incidentsAnterieurs = incident.getUserId() != null
                ? incidentRepository.countByUserId(incident.getUserId())
                : 0;
        if (incidentsAnterieurs >= 3) {
            score += 10;
        } else if (incidentsAnterieurs >= 1) {
            score += 5;
        }

        if (score >= 60) return PrioriteIncident.CRITIQUE;
        if (score >= 40) return PrioriteIncident.HAUTE;
        if (score >= 20) return PrioriteIncident.MOYENNE;
        return PrioriteIncident.FAIBLE;
    }
}
