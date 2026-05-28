package ru.hikeload.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hikeload.domain.UserAccount;
import ru.hikeload.repository.UserAccountRepository;
import ru.hikeload.security.AppUserDetails;
import ru.hikeload.security.JwtService;
import ru.hikeload.web.dto.AuthResponse;
import ru.hikeload.web.dto.LoginRequest;
import ru.hikeload.web.dto.RegisterRequest;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userAccountRepository.existsByEmail(request.email().trim().toLowerCase())) {
            throw new BusinessException("Email уже зарегистрирован");
        }
        UserAccount user = new UserAccount();
        user.setEmail(request.email().trim().toLowerCase());
        user.setDisplayName(request.displayName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        UserAccount saved = userAccountRepository.save(user);
        String token = jwtService.generateToken(saved);
        return AuthResponse.withToken(saved.getId(), saved.getEmail(), saved.getDisplayName(), token);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email().trim().toLowerCase(),
                        request.password()
                )
        );
        AppUserDetails details = (AppUserDetails) auth.getPrincipal();
        String token = jwtService.generateToken(details);
        return AuthResponse.withToken(
                details.getUserId(),
                details.getUsername(),
                details.getDisplayName(),
                token
        );
    }

    public AuthResponse me(AppUserDetails details) {
        return AuthResponse.withoutToken(details.getUserId(), details.getUsername(), details.getDisplayName());
    }
}
