package com.reglog.controller;

import com.reglog.dto.LoginRequest;
import com.reglog.dto.LoginResponse;
import com.reglog.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.cookie-name:token}")
    private String cookieName;

    @Value("${app.cookie.secure:true}")
    private boolean cookieSecure;

    @Value("${app.cookie.samesite:None}")
    private String cookieSameSite;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping({"/login", "/login/"})
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            String token = authService.authenticateAndGenerateToken(request);

            // Create HttpOnly Cookie (1 hour = 3600 seconds)
            ResponseCookie cookie = ResponseCookie.from(cookieName, token)
                    .httpOnly(true)
                    .secure(cookieSecure)
                    .path("/")
                    .maxAge(3600)
                    .sameSite(cookieSameSite)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            LoginResponse loginResponse = new LoginResponse(
                    "success",
                    "Login successful",
                    request.getUsername()
            );

            return ResponseEntity.ok(loginResponse);
        } catch (BadCredentialsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Authentication error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping({"/logout", "/logout/"})
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token != null) {
            authService.revokeToken(token);
        }

        // Clear HttpOnly Cookie
        ResponseCookie clearCookie = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite(cookieSameSite)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());

        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Logged out successfully");

        return ResponseEntity.ok(result);
    }
}
