package tn.comping.spring.backendcomping.ai.dto;

/**
 * Un item de tendance agrégé depuis Reddit, YouTube ou RSS.
 * @param score pertinence/popularité (upvotes, vues normalisées…)
 */
public record TrendItem(String source, String title, String description, int score) {}
