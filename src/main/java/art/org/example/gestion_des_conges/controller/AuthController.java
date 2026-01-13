package art.org.example.gestion_des_conges.controller;

import art.org.example.gestion_des_conges.dto.LoginRequest;
import art.org.example.gestion_des_conges.dto.LoginResponse;
import art.org.example.gestion_des_conges.dto.PasswordResetEmailRequest;
import art.org.example.gestion_des_conges.dto.ResetPasswordRequest;
import art.org.example.gestion_des_conges.security.JwtTokenBlacklist;
import art.org.example.gestion_des_conges.security.JwtTokenProvider;
import art.org.example.gestion_des_conges.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenBlacklist tokenBlacklist;
    private final JwtTokenProvider tokenProvider;

    public AuthController(AuthService authService,
                          JwtTokenBlacklist tokenBlacklist,
                          JwtTokenProvider tokenProvider) {
        this.authService = authService;
        this.tokenBlacklist = tokenBlacklist;
        this.tokenProvider = tokenProvider;
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String token = extractToken(request);
        if (token != null) {
            // Ajouter le token à la blacklist
            tokenBlacklist.addToBlacklist(token);
        }

        // Déconnecter l'utilisateur côté Spring Security
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok("Déconnexion réussie");
    }

    /**
     * Validation du token JWT
     * GET /api/auth/validate
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();

        // 1. Vérifier si le header Authorization est présent
        if (authHeader == null) {
            response.put("valid", false);
            response.put("message", "Authorization header manquant");
            return ResponseEntity.status(401).body(response);
        }

        // 2. Vérifier le format "Bearer <token>"
        if (!authHeader.startsWith("Bearer ")) {
            response.put("valid", false);
            response.put("message", "Format du token invalide");
            return ResponseEntity.status(400).body(response);
        }

        // 3. Extraire le token
        String token = authHeader.substring(7);

        // 4. Vérifier si le token est blacklisté
        if (tokenBlacklist.isBlacklisted(token)) {
            response.put("valid", false);
            response.put("message", "Session expirée. Veuillez vous reconnecter.");
            return ResponseEntity.status(401).body(response);
        }

        // 5. Vérifier la validité technique du token
        if (!tokenProvider.validateToken(token)) {
            response.put("valid", false);
            response.put("message", "Token expiré ou invalide");
            return ResponseEntity.status(401).body(response);
        }

        // 6. Token valide
        response.put("valid", true);
        response.put("message", "Token valide");

        return ResponseEntity.ok(response);
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

    /**
     * Méthode utilitaire pour extraire le token du header
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}