package art.org.example.gestion_des_conges.controller;

import art.org.example.gestion_des_conges.dto.TeamDTO;
import art.org.example.gestion_des_conges.service.TeamService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teams")
@PreAuthorize("hasRole('ADMIN')") // Seul ADMIN peut gérer les équipes
public class TeamController {

    @Autowired
    private TeamService teamService;

    /**
     * US-02.5 : Créer une nouvelle équipe
     */
    @PostMapping
    public ResponseEntity<TeamDTO> createTeam(@Valid @RequestBody CreateTeamRequest request) {
        TeamDTO createdTeam = teamService.createTeam(request.getName(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTeam);
    }

    /**
     * US-02.5 : Récupérer toutes les équipes
     */
    @GetMapping
    public ResponseEntity<List<TeamDTO>> getAllTeams(
            @RequestParam(required = false) String search) {

        List<TeamDTO> teams;
        if (search != null && !search.trim().isEmpty()) {
            teams = teamService.searchTeams(search);
        } else {
            teams = teamService.getAllTeams();
        }

        return ResponseEntity.ok(teams);
    }

    /**
     * US-02.5 : Récupérer une équipe par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TeamDTO> getTeamById(@PathVariable Long id) {
        TeamDTO team = teamService.getTeamById(id);
        return ResponseEntity.ok(team);
    }

    /**
     * US-02.5 : Modifier une équipe
     */
    @PutMapping("/{id}")
    public ResponseEntity<TeamDTO> updateTeam(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTeamRequest request) {

        TeamDTO updatedTeam = teamService.updateTeam(id, request.getName(), request.getDescription());
        return ResponseEntity.ok(updatedTeam);
    }

    /**
     * US-02.5 : Supprimer une équipe
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * US-02.5 : Ajouter un membre à une équipe
     */
    @PostMapping("/{teamId}/members/{userId}")
    public ResponseEntity<TeamDTO> addMember(
            @PathVariable Long teamId,
            @PathVariable Long userId) {

        TeamDTO updatedTeam = teamService.addMember(teamId, userId);
        return ResponseEntity.ok(updatedTeam);
    }

    /**
     * US-02.5 : Retirer un membre d'une équipe
     */
    @DeleteMapping("/{teamId}/members/{userId}")
    public ResponseEntity<TeamDTO> removeMember(
            @PathVariable Long teamId,
            @PathVariable Long userId) {

        TeamDTO updatedTeam = teamService.removeMember(teamId, userId);
        return ResponseEntity.ok(updatedTeam);
    }

    /**
     * US-02.6 : Désigner un validateur pour une équipe
     */
    @PutMapping("/{teamId}/validator")
    public ResponseEntity<TeamDTO> setValidator(
            @PathVariable Long teamId,
            @RequestBody SetValidatorRequest request) {

        TeamDTO updatedTeam = teamService.setValidator(teamId, request.getUserId());
        return ResponseEntity.ok(updatedTeam);
    }

    /**
     * US-02.6 : Retirer le validateur d'une équipe
     */
    @DeleteMapping("/{teamId}/validator")
    public ResponseEntity<TeamDTO> removeValidator(@PathVariable Long teamId) {
        TeamDTO updatedTeam = teamService.removeValidator(teamId);
        return ResponseEntity.ok(updatedTeam);
    }

    /**
     * Récupérer les équipes sans validateur
     */
    @GetMapping("/without-validator")
    public ResponseEntity<List<TeamDTO>> getTeamsWithoutValidator() {
        List<TeamDTO> teams = teamService.getTeamsWithoutValidator();
        return ResponseEntity.ok(teams);
    }

    /**
     * Récupérer les équipes vides
     */
    @GetMapping("/empty")
    public ResponseEntity<List<TeamDTO>> getEmptyTeams() {
        List<TeamDTO> teams = teamService.getEmptyTeams();
        return ResponseEntity.ok(teams);
    }

    /**
     * Récupérer les équipes d'un validateur
     */
    @GetMapping("/validator/{validatorId}")
    public ResponseEntity<List<TeamDTO>> getTeamsByValidator(@PathVariable Long validatorId) {
        List<TeamDTO> teams = teamService.getTeamsByValidator(validatorId);
        return ResponseEntity.ok(teams);
    }

    /**
     * Récupérer toutes les équipes avec le nombre de membres
     */
    @GetMapping("/with-member-count")
    public ResponseEntity<List<TeamDTO>> getAllTeamsWithMemberCount() {
        List<TeamDTO> teams = teamService.getAllTeamsWithMemberCount();
        return ResponseEntity.ok(teams);
    }

    /**
     * Vérifier la disponibilité d'un nom d'équipe
     */
    @GetMapping("/check-name-availability")
    public ResponseEntity<Map<String, Boolean>> checkTeamNameAvailability(
            @RequestParam @NotBlank String name,
            @RequestParam(required = false) Long excludeTeamId) {

        boolean isAvailable = teamService.isTeamNameAvailable(name, excludeTeamId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", isAvailable);
        return ResponseEntity.ok(response);
    }

    // ========== DTOs pour les requêtes ==========

    @Data
    public static class CreateTeamRequest {
        @NotBlank(message = "Le nom de l'équipe est obligatoire")
        private String name;

        private String description;
    }

    @Data
    public static class UpdateTeamRequest {
        @NotBlank(message = "Le nom de l'équipe est obligatoire")
        private String name;

        private String description;
    }

    @Data
    public static class SetValidatorRequest {
        @NotBlank(message = "L'ID de l'utilisateur est obligatoire")
        private Long userId;
    }
}