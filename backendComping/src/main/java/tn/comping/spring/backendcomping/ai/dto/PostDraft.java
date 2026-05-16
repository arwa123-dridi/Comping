package tn.comping.spring.backendcomping.ai.dto;

import java.util.List;

/** Brouillon de post complet généré par le LLM. */
public record PostDraft(String title, String content, List<String> hashtags) {}
