package tn.comping.spring.backendcomping.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.services.serviceImpl.SignupService;
import tn.comping.spring.backendcomping.dto.SignupDTO;

@RestController
@RequestMapping("/api/auth")

public class SignupController {

    @Autowired
    private SignupService signupService;

    // Endpoint to register a new user
    @PostMapping("/registerUser")
    public ResponseEntity<?> registerUser(@RequestBody SignupDTO dto) {
        try {
            SignupEntity newUser = signupService.registerUser(dto);
            return ResponseEntity.ok(newUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}