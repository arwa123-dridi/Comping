package tn.comping.spring.backendcomping.services;

import tn.comping.spring.backendcomping.dto.ConventionPartenaireRequest;
import tn.comping.spring.backendcomping.dto.ConventionPartenaireResponse;
import java.util.List;

public interface ConventionPartenaireService {
    List<ConventionPartenaireResponse> getAllConventions();
    ConventionPartenaireResponse getConventionById(String id);
    ConventionPartenaireResponse createConvention(ConventionPartenaireRequest request);
    ConventionPartenaireResponse updateConvention(String id, ConventionPartenaireRequest request);
    void deleteConvention(String id);
}