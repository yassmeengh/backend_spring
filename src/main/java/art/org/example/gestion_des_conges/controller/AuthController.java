package art.org.example.gestion_des_conges.controller;

import art.org.example.gestion_des_conges.dto.LoginRequest;
import art.org.example.gestion_des_conges.dto.LoginResponse;
import art.org.example.gestion_des_conges.dto.PasswordResetEmailRequest;
import art.org.example.gestion_des_conges.dto.ResetPasswordRequest;
import art.org.example.gestion_des_conges.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * US-01.1 : Connexion avec identifiant et mot de passe
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * US-01.2 : Déconnexion sécurisée
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        authService.logout();
        return ResponseEntity.ok("Logout successful");
    }

    /**
     * US-01.3 : Réinitialisation mot de passe - Demande de reset
     * POST /api/auth/reset-password-request
     */
    @PostMapping("/reset-password-request")
    public ResponseEntity<String> requestPasswordReset(@RequestBody PasswordResetEmailRequest request) {
        try {
            String token = authService.requestPasswordReset(request.getEmail());
            return ResponseEntity.ok("Password reset email sent. Token: " + token);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * US-01.3 : Réinitialisation mot de passe - Reset avec token
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok("Password reset successful");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Test endpoint pour vérifier si l'utilisateur est authentifié
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            var user = authService.getCurrentUser();
            return ResponseEntity.ok(new LoginResponse(
                    null,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRole().name()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
    }
}