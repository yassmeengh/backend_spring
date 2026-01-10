package art.org.example.gestion_des_conges.service;

import art.org.example.gestion_des_conges.dto.LoginRequest;
import art.org.example.gestion_des_conges.dto.LoginResponse;
import art.org.example.gestion_des_conges.dto.ResetPasswordRequest;
import art.org.example.gestion_des_conges.entity.User;
import art.org.example.gestion_des_conges.repository.UserRepository;
import art.org.example.gestion_des_conges.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * US-01.1 : Connexion avec identifiant et mot de passe
     */
    public LoginResponse login(LoginRequest loginRequest) {
        System.out.println("=== DEBUG: AuthService.login() START ===");
        System.out.println("Username: " + loginRequest.getUsername());

        try {
            // 1. Authentification
            System.out.println("1. Authenticating...");
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            System.out.println("2. Authentication SUCCESS!");

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 2. Génération du token
            System.out.println("3. Generating token...");
            String token = tokenProvider.generateToken(authentication);
            System.out.println("4. Token generated: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "NULL"));

            // 3. Récupération user
            System.out.println("5. Getting user from authentication...");
            User user = (User) authentication.getPrincipal();
            System.out.println("6. User: " + user.getUsername() + ", ID: " + user.getId());

            // 4. Création réponse
            System.out.println("7. Creating LoginResponse...");
            LoginResponse response = new LoginResponse(
                    token,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRole().name()
            );
            System.out.println("8. Response created!");
            System.out.println("=== DEBUG: AuthService.login() END ===");

            return response;

        } catch (Exception e) {
            System.out.println("❌ ERROR in login: " + e.getClass().getName());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }

    /**
     * US-01.2 : Déconnexion sécurisée
     */
    public void logout() {
        SecurityContextHolder.clearContext();
    }

    /**
     * US-01.3 : Réinitialisation mot de passe - Étape 1: Demande de reset
     */
    @Transactional
    public String requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(24)); // Token valide 24h

        userRepository.save(user);

        // TODO: Envoyer email avec le lien de réinitialisation
        // emailService.sendPasswordResetEmail(user.getEmail(), resetToken);

        return resetToken;
    }

    /**
     * US-01.3 : Réinitialisation mot de passe - Étape 2: Reset avec token
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);
    }

    /**
     * Récupère l'utilisateur actuellement connecté
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }
        return (User) authentication.getPrincipal();
    }
}