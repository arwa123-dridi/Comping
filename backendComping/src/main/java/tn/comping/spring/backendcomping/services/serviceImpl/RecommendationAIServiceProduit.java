package tn.comping.spring.backendcomping.services.serviceImpl;

import java.util.List;

public interface RecommendationAIServiceProduit {

    List<String> getRecommendations(List<String> cartProducts);
}