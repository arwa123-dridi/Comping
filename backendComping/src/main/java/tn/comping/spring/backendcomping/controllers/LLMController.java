package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tn.comping.spring.backendcomping.services.serviceImpl.LLMService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@CrossOrigin("*")
public class LLMController {

    private final LLMService llmService;

    @PostMapping("/message")
    public SseEmitter chat(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        List<Map<String, String>> history = (List<Map<String, String>>) request.get("history");
        
        SseEmitter emitter = new SseEmitter(60000L); // 1 minute timeout
        llmService.streamChat(message, history, emitter);
        return emitter;
    }
}
