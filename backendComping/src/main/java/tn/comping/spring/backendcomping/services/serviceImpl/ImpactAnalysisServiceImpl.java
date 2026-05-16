package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.dto.ImpactAnalysisResponse;
import tn.comping.spring.backendcomping.dto.PredictionResponse;
import tn.comping.spring.backendcomping.entities.Incident;
import tn.comping.spring.backendcomping.entities.IncidentPattern;
import tn.comping.spring.backendcomping.entities.IncidentImpactRelation;
import tn.comping.spring.backendcomping.repositories.IncidentRepository;
import tn.comping.spring.backendcomping.repositories.IncidentPatternRepository;
import tn.comping.spring.backendcomping.repositories.IncidentImpactRelationRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImpactAnalysisServiceImpl implements ImpactAnalysisService {

    private final IncidentRepository incidentRepository;
    private final IncidentPatternRepository patternRepository;
    private final IncidentImpactRelationRepository impactRelationRepository;

    @Override
    public ImpactAnalysisResponse analyzeIncidentImpact(String incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident not found: " + incidentId));

        // Find related incidents (impact relations)
        List<IncidentImpactRelation> relations = impactRelationRepository.findByCauseIncidentId(incidentId);
        
        ImpactAnalysisResponse response = ImpactAnalysisResponse.builder()
                .incidentId(incidentId)
                .incidentType(incident.getType())
                .directlyAffected(new HashSet<>(relations.size()))
                .totalPotentialImpact(relations.size())
                .averageImpactStrength(relations.stream()
                        .mapToDouble(IncidentImpactRelation::getImpactStrength)
                        .average()
                        .orElse(0.0))
                .build();

        // Add affected incidents
        for (IncidentImpactRelation rel : relations) {
            response.getDirectlyAffected().add(rel.getAffectedIncidentId());
        }

        return response;
    }

    @Override
    public List<PredictionResponse> getPredictionsForIncident(String incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident not found: " + incidentId));

        // Find patterns that match this incident type
        List<IncidentPattern> applicablePatterns = patternRepository.findByTriggerIncidentTypesContaining(incident.getType())
                .stream()
                .filter(IncidentPattern::getEnabled)
                .collect(Collectors.toList());

        List<PredictionResponse> predictions = new ArrayList<>();
        
        for (IncidentPattern pattern : applicablePatterns) {
            if (pattern.getConfidenceScore() >= 0.6) { // Only high-confidence predictions
                predictions.add(PredictionResponse.builder()
                        .patternId(pattern.getId())
                        .triggerIncidentType(incident.getType())
                        .predictedIncidentType(pattern.getPredictedIncidentType())
                        .confidenceScore(pattern.getConfidenceScore())
                        .estimatedTimeToOccurMinutes(pattern.getTimeToOccurMinutes())
                        .occurrenceHistory(pattern.getOccurrenceCount())
                        .recommendation("Monitor for " + pattern.getPredictedIncidentType() + 
                                      " incidents within " + pattern.getTimeToOccurMinutes() + " minutes")
                        .build());
            }
        }

        return predictions;
    }

    @Override
    @Scheduled(fixedDelay = 600000) // Every 10 minutes
    public void detectPatterns() {
        log.info("Running pattern detection...");
        
        // Get all incidents from last 30 days
        List<Incident> recentIncidents = incidentRepository.findAll().stream()
                .filter(i -> i.getDateDeclaration() != null)
                .collect(Collectors.toList());

        // Analyze incident pairs for patterns
        for (int i = 0; i < recentIncidents.size(); i++) {
            for (int j = i + 1; j < recentIncidents.size(); j++) {
                analyzeIncidentPair(recentIncidents.get(i), recentIncidents.get(j));
            }
        }
    }

    private void analyzeIncidentPair(Incident incident1, Incident incident2) {
        // Check if incident1 happened before incident2
        if (incident1.getDateDeclaration().before(incident2.getDateDeclaration())) {
            // Calculate time difference
            long timeDiffMinutes = java.time.temporal.ChronoUnit.MINUTES.between(
                    new java.sql.Timestamp(incident1.getDateDeclaration().getTime()).toLocalDateTime(),
                    new java.sql.Timestamp(incident2.getDateDeclaration().getTime()).toLocalDateTime()
            );

            // If within 4 hours, might be related
            if (timeDiffMinutes > 0 && timeDiffMinutes < 240) {
                // Record impact relation
                IncidentImpactRelation relation = IncidentImpactRelation.builder()
                        .causeIncidentId(incident1.getIdIncident())
                        .affectedIncidentId(incident2.getIdIncident())
                        .impactStrength(calculateImpactStrength(incident1, incident2))
                        .delayMinutes((int) timeDiffMinutes)
                        .relationshipType("INDIRECT")
                        .description(incident1.getType() + " may have triggered " + incident2.getType())
                        .occurrenceCount(1)
                        .detectedAt(LocalDateTime.now())
                        .confirmed(false)
                        .build();

                impactRelationRepository.save(relation);

                // Update or create pattern
                updatePattern(incident1.getType(), incident2.getType(), (int) timeDiffMinutes);
            }
        }
    }

    private double calculateImpactStrength(Incident cause, Incident effect) {
        double strength = 0.5; // Base strength
        
        // Increase if caused incident has higher impact score
        if (cause.getImpactScore() != null && cause.getImpactScore() > 7) {
            strength += 0.2;
        }
        
        // Increase if same location
        if (cause.getLocation() != null && cause.getLocation().equals(effect.getLocation())) {
            strength += 0.1;
        }
        
        // Increase if both are same category
        if (cause.getCategorie() != null && cause.getCategorie().equals(effect.getCategorie())) {
            strength += 0.05;
        }
        
        return Math.min(strength, 1.0);
    }

    private void updatePattern(String triggerType, String predictedType, int delayMinutes) {
        List<IncidentPattern> existing = patternRepository.findByTriggerIncidentTypesContaining(triggerType).stream()
                .filter(p -> p.getPredictedIncidentType().equals(predictedType))
                .collect(Collectors.toList());

        if (!existing.isEmpty()) {
            IncidentPattern pattern = existing.get(0);
            pattern.setOccurrenceCount(pattern.getOccurrenceCount() + 1);
            pattern.setLastDetected(LocalDateTime.now());
            
            // Update confidence score based on occurrences
            pattern.setConfidenceScore(Math.min(0.95, 0.5 + (pattern.getOccurrenceCount() * 0.05)));
            
            // Update average delay
            if (pattern.getTimeToOccurMinutes() != null) {
                pattern.setTimeToOccurMinutes((pattern.getTimeToOccurMinutes() + delayMinutes) / 2);
            } else {
                pattern.setTimeToOccurMinutes(delayMinutes);
            }
            
            patternRepository.save(pattern);
        } else {
            // Create new pattern
            IncidentPattern newPattern = IncidentPattern.builder()
                    .triggerIncidentTypes(new java.util.ArrayList<>(java.util.Arrays.asList(triggerType)))
                    .predictedIncidentType(predictedType)
                    .confidenceScore(0.5)
                    .occurrenceCount(1)
                    .timeToOccurMinutes(delayMinutes)
                    .firstDetected(LocalDateTime.now())
                    .lastDetected(LocalDateTime.now())
                    .enabled(true)
                    .build();
            
            patternRepository.save(newPattern);
        }
    }

    @Override
    public List<IncidentPattern> getActivePatterns() {
        return patternRepository.findByEnabledIsTrue();
    }
}
