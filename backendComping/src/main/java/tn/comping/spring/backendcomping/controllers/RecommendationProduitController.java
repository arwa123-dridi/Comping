package tn.comping.spring.backendcomping.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import tn.comping.spring.backendcomping.services.serviceImpl.RecommendationAIServiceProduit;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationProduitController {
    private final RecommendationAIServiceProduit recommendationService;

    @PostMapping("/getRecommendation")
    public ResponseEntity<List<String>> getRecommendations(@RequestBody List<String> cartProducts) {

        List<String> recommendations = recommendationService.getRecommendations(cartProducts);

        return ResponseEntity.ok(recommendations);
    }
}
