package tn.comping.spring.backendcomping.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tn.comping.spring.backendcomping.dto.LoginDTORequest;
import tn.comping.spring.backendcomping.dto.LoginDTOResponse;
import tn.comping.spring.backendcomping.services.serviceImpl.SignupService;
import tn.comping.spring.backendcomping.utils.Constants;



@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@Slf4j
public class AuthController {
    private  final SignupService signupService;
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginDTORequest dto) {
        try {
            LoginDTOResponse response = signupService.login(dto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
