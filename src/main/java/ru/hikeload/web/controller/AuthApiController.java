package ru.hikeload.web.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hikeload.security.CurrentUserService;
import ru.hikeload.service.AuthService;
import ru.hikeload.web.dto.AuthResponse;
import ru.hikeload.web.dto.LoginRequest;
import ru.hikeload.web.dto.RegisterRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthService authService;
    private final CurrentUserService currentUserService;

    public AuthApiController(AuthService authService, CurrentUserService currentUserService) {
        this.authService = authService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest body) {
        return authService.register(body);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest body) {
        return authService.login(body);
    }

    @GetMapping("/me")
    public AuthResponse me() {
        return authService.me(currentUserService.getCurrentUser());
    }

    /** JWT stateless: клиент удаляет токен локально */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
    }
}
