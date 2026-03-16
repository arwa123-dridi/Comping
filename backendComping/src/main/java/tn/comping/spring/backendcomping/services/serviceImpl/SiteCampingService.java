package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.*;
import java.util.List;

public interface SiteCampingService {
    List<SiteCampingResponse> getAll();
    SiteCampingResponse getById(String id);
    SiteCampingResponse create(SiteCampingRequest request);
    SiteCampingResponse update(String id, SiteCampingRequest request);
    void delete(String id);
    List<SiteCampingResponse> getDisponibles();
    List<SiteCampingResponse> getByLocalisation(String localisation);
    List<SiteCampingResponse> getByProprietaire(String proprietaireId);
    List<SiteCampingResponse> filtrer(Double prixMin, Double prixMax, Boolean disponible);
}