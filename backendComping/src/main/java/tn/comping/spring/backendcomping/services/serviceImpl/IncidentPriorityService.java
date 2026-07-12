package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.entities.Incident;
import tn.comping.spring.backendcomping.entities.PrioriteIncident;

public interface IncidentPriorityService {
    PrioriteIncident classify(Incident incident);
}
