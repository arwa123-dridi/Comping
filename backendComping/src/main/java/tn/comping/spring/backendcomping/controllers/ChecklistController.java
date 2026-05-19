package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.ChecklistResponseDTO;
import tn.comping.spring.backendcomping.services.serviceImpl.AIChecklistService;

import java.util.Map;

@RestController
@RequestMapping("/api/checklist")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ChecklistController {

    private final AIChecklistService checklistService;

    @PostMapping("/generate")
    public ResponseEntity<ChecklistResponseDTO> generateChecklist(@RequestBody Map<String, Object> request) {
        String destination = (String) request.get("destination");
        int duration = (int) request.get("durationDays");
        String difficulty = (String) request.get("difficulty");
        String season = (String) request.get("season");
        
        return ResponseEntity.ok(checklistService.generateChecklist(destination, duration, difficulty, season));
    }
}
