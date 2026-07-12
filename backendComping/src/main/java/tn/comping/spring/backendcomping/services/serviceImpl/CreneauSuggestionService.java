package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.CreneauSuggestionResponse;

public interface CreneauSuggestionService {
    CreneauSuggestionResponse suggest(String demandeTransportId);
}
