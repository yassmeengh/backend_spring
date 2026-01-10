package art.org.example.gestion_des_conges.controller;

import art.org.example.gestion_des_conges.dto.CreateUserRequest;
import art.org.example.gestion_des_conges.dto.UpdateUserRequest;
import art.org.example.gestion_des_conges.dto.UserDTO;
import art.org.example.gestion_des_conges.entity.User;
import art.org.example.gestion_des_conges.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')") // Seul ADMIN peut gérer les utilisateurs
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * US-02.1 : Créer un utilisateur
     */
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDTO createdUser = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * US-02.3 : Liste des utilisateurs avec recherche
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) User.Role role,
            @RequestParam(required = false) Long teamId) {

        List<UserDTO> users = userService.searchUsers(search, role, teamId);
        return ResponseEntity.ok(users);
    }

    /**
     * US-02.3 : Récupérer un utilisateur par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * US-02.2 : Modifier un utilisateur
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        UserDTO updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * US-02.2 : Activer/Désactiver un utilisateur
     */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<UserDTO> toggleUserStatus(@PathVariable Long id) {
        UserDTO updatedUser = userService.toggleUserStatus(id);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * US-02.4 : Changer le rôle d'un utilisateur
     */
    @PatchMapping("/{id}/role")
    public ResponseEntity<UserDTO> changeUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        User.Role newRole = User.Role.valueOf(request.get("role"));
        UserDTO updatedUser = userService.changeUserRole(id, newRole);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Récupérer les utilisateurs par équipe
     */
    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<UserDTO>> getUsersByTeam(@PathVariable Long teamId) {
        List<UserDTO> users = userService.getUsersByTeam(teamId);
        return ResponseEntity.ok(users);
    }

    /**
     * Récupérer les utilisateurs sans équipe
     */
    @GetMapping("/without-team")
    public ResponseEntity<List<UserDTO>> getUsersWithoutTeam() {
        List<UserDTO> users = userService.getUsersWithoutTeam();
        return ResponseEntity.ok(users);
    }

    /**
     * Récupérer les validateurs disponibles
     */
    @GetMapping("/available-validators")
    public ResponseEntity<List<UserDTO>> getAvailableValidators() {
        List<UserDTO> validators = userService.getAvailableValidateurs();
        return ResponseEntity.ok(validators);
    }

    /**
     * Vérifier la disponibilité d'un email
     */
    @GetMapping("/check-email-availability")
    public ResponseEntity<Map<String, Boolean>> checkEmailAvailability(
            @RequestParam @NotBlank String email,
            @RequestParam(required = false) Long excludeUserId) {

        boolean isAvailable = userService.isEmailAvailable(email, excludeUserId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", isAvailable);
        return ResponseEntity.ok(response);
    }

    /**
     * Vérifier la disponibilité d'un username
     */
    @GetMapping("/check-username-availability")
    public ResponseEntity<Map<String, Boolean>> checkUsernameAvailability(
            @RequestParam @NotBlank String username,
            @RequestParam(required = false) Long excludeUserId) {

        boolean isAvailable = userService.isUsernameAvailable(username, excludeUserId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", isAvailable);
        return ResponseEntity.ok(response);
    }
}