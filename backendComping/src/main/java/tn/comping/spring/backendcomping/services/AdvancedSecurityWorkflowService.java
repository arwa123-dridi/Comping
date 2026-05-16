package tn.comping.spring.backendcomping.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.entities.*;
import tn.comping.spring.backendcomping.repositories.AlerteRepository;
import tn.comping.spring.backendcomping.repositories.SecuriteRepository;
import tn.comping.spring.backendcomping.repositories.UrgenceRepository;
import tn.comping.spring.backendcomping.services.serviceImpl.LLMService;

import java.util.*;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

@Service
@Transactional
public class AdvancedSecurityWorkflowService {

    @Autowired
    private AlerteRepository alerteRepository;

    @Autowired
    private UrgenceRepository urgenceRepository;

    @Autowired
    private SecuriteRepository securiteRepository;

    @Autowired
    private LLMService llmService; // Assumes LLMService exists

    // 1. Automatic Alert Escalation Workflow (Alerte -> Urgence)
    @Scheduled(fixedRate = 60000)
    public List<Urgence> escalatePendingAlerts() {
        List<Alerte> pendingAlerts = alerteRepository.findByStatut("PENDING");
        List<Urgence> newUrgences = new ArrayList<>();
        
        Date now = new Date();
        for (Alerte alerte : pendingAlerts) {
            // DEMO MODE: Escalation immediate (0 min timeout) instead of 15 * 60 * 1000
            if (alerte.getDateDeclenchement() != null && 
                now.getTime() - alerte.getDateDeclenchement().getTime() >= 0) {
                
                // Escalate to Urgence
                Urgence urgence = new Urgence();
                urgence.setTitre("ESCALATED: " + alerte.getTitre());
                urgence.setDescription(alerte.getDescription());
                urgence.setNiveauUrgence("HAUTE");
                urgence.setStatut("ATTENDANT");
                urgence.setDateCreation(now);
                urgence.setSiteCampingId(alerte.getSiteCampingId());
                urgence.setNotes("Auto-escalated from Alerte " + alerte.getId());
                
                urgenceRepository.save(urgence);
                newUrgences.add(urgence);
                
                alerte.setStatut("ESCALATED");
                alerte.setResolution("Automatiquement escaladée en Urgence - Timeout 15m");
                alerteRepository.save(alerte);
            }
        }
        return newUrgences;
    }

    // 2. Smart Security Dispatching (Urgence -> Securite)
    public Securite dispatchSecurityForUrgence(String urgenceId) {
        Optional<Urgence> urgenceOpt = urgenceRepository.findById(urgenceId);
        if (urgenceOpt.isEmpty()) return null;
        
        Urgence urgence = urgenceOpt.get();
        
        // Logic to find best security agent/team (simplified to creating a new task)
        Securite task = new Securite();
        task.setTitre("DÉPLOIEMENT URGENCE: " + urgence.getTitre());
        task.setDescription("Check emergency at " + urgence.getLocation());
        task.setNiveauSecurite("CRITIQUE");
        task.setStatut("EN_COURS");
        task.setDateCreation(new Date());
        task.setDateDebut(new Date());
        task.setSiteCampingId(urgence.getSiteCampingId());
        task.setTypeMesure("PATROUILLE");
        task.setRelatedIncidentIds(Collections.singletonList(urgence.getId()));
        
        return securiteRepository.save(task);
    }

    // 3. Predictive AI Security Analysis
    public String runPredictiveAnalysis(String siteCampingId) {
        List<Alerte> alertes = alerteRepository.findBySiteCampingId(siteCampingId);
        StringBuilder data = new StringBuilder("Recent alerts data: ");
        for (int i = 0; i < Math.min(alertes.size(), 20); i++) {
            data.append(alertes.get(i).getTitre()).append(" (")
                .append(alertes.get(i).getPosition()).append(") - ");
        }
        
        // Pass to LLM Service for processing
        String prompt = "Analyser ces alertes et recommander des patrouilles: " + data.toString();
        tn.comping.spring.backendcomping.dto.ChatMessageResponse response = llmService.analyzeIncident(prompt);
        return response != null ? response.getResponse() : "Erreur d'analyse AI";
    }

    // 4. Campus-wide Emergency Broadcast
    public void broadcastCodeRed(String message) {
        // Normally, we'd use SimpMessagingTemplate here.
        // E.g. messagingTemplate.convertAndSend("/topic/emergencies", new BroadcastMessage(message, "CODE_RED"));
        System.out.println("🚨 BROADCAST EMERGENCY: " + message);
        // Save broadcast event as a critical Urgence
        Urgence broadcast = new Urgence();
        broadcast.setTitre("BROADCAST CODE RED");
        broadcast.setDescription(message);
        broadcast.setNiveauUrgence("CRITIQUE");
        broadcast.setStatut("ACTIVE");
        broadcast.setDateCreation(new Date());
        urgenceRepository.save(broadcast);
    }

    // 5. Geofencing & Proximity Alerts (Securite -> Alerte)
    public Alerte checkGeofenceBreach(Double lat, Double lon, String siteId, String userId) {
        // Mock geofence logic: check if lat/lon is inside restricted bounding box
        boolean isBreach = (lat != null && lat > 36.8 && lon != null && lon > 10.1); 
        
        if (isBreach) {
            Alerte alerte = new Alerte();
            alerte.setTitre("Alerte de Proximité / Géorepérage");
            alerte.setDescription("Intrusion détectée pour l'utilisateur: " + userId + " aux coordonnées: " + lat + "," + lon);
            alerte.setDateDeclenchement(new Date());
            alerte.setStatut("PENDING");
            alerte.setPriorite("HAUTE");
            alerte.setSiteCampingId(siteId);
            alerte.setPosition(lat + "," + lon);
            return alerteRepository.save(alerte);
        }
        return null;
    }

    // 6. Automated Post-Mortem Reports (Urgence closure)
    public String generatePostMortem(String urgenceId) {
        Optional<Urgence> urgenceOpt = urgenceRepository.findById(urgenceId);
        if (urgenceOpt.isEmpty()) return "Urgence non trouvée";
        
        Urgence u = urgenceOpt.get();
        if (!"RESOLVED".equalsIgnoreCase(u.getStatut()) && !"COMPLETEE".equalsIgnoreCase(u.getStatut())) {
            u.setStatut("COMPLETEE");
            u.setDateResolution(new Date());
            urgenceRepository.save(u);
        }
        
        String prompt = "Générer un rapport post-mortem formel pour l'urgence suivante:"
                + " Titre: " + u.getTitre() 
                + " Desc: " + u.getDescription()
                + " Resolution: " + (u.getResolution() != null ? u.getResolution() : "Inconnue");
                
        tn.comping.spring.backendcomping.dto.ChatMessageResponse resp = llmService.analyzeIncident(prompt);
        String report = resp != null ? resp.getResponse() : "Erreur génération rapport";
        u.setNotes("Post-Mortem: " + report);
        urgenceRepository.save(u);
        return report;
    }
}
