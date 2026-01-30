package root.cyb.mhr.CustomAuthentication.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import root.cyb.mhr.CustomAuthentication.dto.AuthResponse;
import root.cyb.mhr.CustomAuthentication.dto.LoginRequest;
import root.cyb.mhr.CustomAuthentication.dto.RegisterRequest;
import root.cyb.mhr.CustomAuthentication.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(e.getMessage()));
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<AuthResponse> verifyEmail(@RequestParam String token) {
        try {
            AuthResponse response = authService.verifyAccount(token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(e.getMessage()));
        }
    }

    @GetMapping("/test")
    public ResponseEntity<AuthResponse> testProtectedEndpoint() {
        return ResponseEntity.ok(new AuthResponse("This is a protected endpoint. You are authenticated!"));
    }
}
