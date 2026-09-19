package com.reglog.service;

import com.reglog.dto.LoginRequest;
import com.reglog.entity.JwtToken;
import com.reglog.entity.User;
import com.reglog.repository.JwtTokenRepository;
import com.reglog.repository.UserRepository;
import com.reglog.security.JwtUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenRepository jwtTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository,
                       JwtTokenRepository jwtTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.jwtTokenRepository = jwtTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @Transactional
    public String authenticateAndGenerateToken(LoginRequest request) {
        User user = userRepository.findByName(request.getUsername().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        // Generate JWT (1 hour validity)
        String token = jwtUtils.generateToken(user.getName(), user.getId());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(1);

        JwtToken jwtToken = new JwtToken(user, token, now, expiresAt);
        jwtTokenRepository.save(jwtToken);

        return token;
    }

    @Transactional
    public void revokeToken(String token) {
        if (token != null && !token.isBlank()) {
            jwtTokenRepository.deleteByToken(token);
        }
    }
}
