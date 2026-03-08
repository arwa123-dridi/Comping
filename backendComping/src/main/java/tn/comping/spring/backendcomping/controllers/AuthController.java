package tn.comping.spring.backendcomping.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.comping.spring.backendcomping.utils.mapper.Constants;

@RestController
@RequestMapping(Constants.APP_ROOT+Constants.AUTH)
@AllArgsConstructor
@Slf4j
public class AuthController {
}
