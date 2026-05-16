package tn.comping.spring.backendcomping.ai.dto;

import java.util.List;

/** Réponse JSON de Groq pour la liste de sujets. */
public record TopicsResponse(List<String> topics) {}
